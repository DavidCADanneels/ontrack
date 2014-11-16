package net.nemerosa.ontrack.extension.git.service;

import com.google.common.collect.Lists;
import net.nemerosa.ontrack.extension.api.model.BuildDiffRequest;
import net.nemerosa.ontrack.extension.git.client.*;
import net.nemerosa.ontrack.extension.git.model.*;
import net.nemerosa.ontrack.extension.git.property.GitBranchConfigurationProperty;
import net.nemerosa.ontrack.extension.git.property.GitBranchConfigurationPropertyType;
import net.nemerosa.ontrack.extension.issues.IssueServiceRegistry;
import net.nemerosa.ontrack.extension.issues.model.ConfiguredIssueService;
import net.nemerosa.ontrack.extension.issues.model.Issue;
import net.nemerosa.ontrack.extension.issues.model.IssueServiceConfigurationRepresentation;
import net.nemerosa.ontrack.extension.issues.model.IssueServiceNotConfiguredException;
import net.nemerosa.ontrack.extension.scm.model.SCMBuildView;
import net.nemerosa.ontrack.extension.scm.model.SCMChangeLogFileChangeType;
import net.nemerosa.ontrack.extension.scm.service.AbstractSCMChangeLogService;
import net.nemerosa.ontrack.model.Ack;
import net.nemerosa.ontrack.model.job.*;
import net.nemerosa.ontrack.model.security.SecurityService;
import net.nemerosa.ontrack.model.structure.*;
import net.nemerosa.ontrack.model.support.MessageAnnotationUtils;
import net.nemerosa.ontrack.model.support.MessageAnnotator;
import net.nemerosa.ontrack.tx.Transaction;
import net.nemerosa.ontrack.tx.TransactionService;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
public class GitServiceImpl extends AbstractSCMChangeLogService<GitConfiguration, GitBuildInfo, GitChangeLogIssue> implements GitService, JobProvider {

    private final Logger logger = LoggerFactory.getLogger(GitService.class);
    private final Collection<GitConfigurator> configurators;
    private final GitClientFactory gitClientFactory;
    private final PropertyService propertyService;
    private final IssueServiceRegistry issueServiceRegistry;
    private final JobQueueService jobQueueService;
    private final SecurityService securityService;
    private final TransactionService transactionService;

    @Autowired
    public GitServiceImpl(
            StructureService structureService,
            PropertyService propertyService,
            Collection<GitConfigurator> configurators,
            GitClientFactory gitClientFactory,
            IssueServiceRegistry issueServiceRegistry,
            JobQueueService jobQueueService,
            SecurityService securityService,
            TransactionService transactionService) {
        super(structureService, propertyService);
        this.configurators = configurators;
        this.gitClientFactory = gitClientFactory;
        this.propertyService = propertyService;
        this.issueServiceRegistry = issueServiceRegistry;
        this.jobQueueService = jobQueueService;
        this.securityService = securityService;
        this.transactionService = transactionService;
    }

    @Override
    public void forEachConfiguredBranch(BiConsumer<Branch, GitConfiguration> consumer) {
        for (Project project : structureService.getProjectList()) {
            structureService.getBranchesForProject(project.getId()).stream()
                    .filter(branch -> branch.getType() != BranchType.TEMPLATE_DEFINITION)
                    .forEach(branch -> {
                        GitConfiguration configuration = getBranchConfiguration(branch);
                        if (configuration.isValid()) {
                            consumer.accept(branch, configuration);
                        }
                    });
        }
    }

    @Override
    public Collection<Job> getJobs() {
        Collection<Job> jobs = new ArrayList<>();
        forEachConfiguredBranch((branch, configuration) -> {
            // Indexation job
            if (configuration.getIndexationInterval() > 0) {
                jobs.add(createIndexationJob(branch, configuration));
            }
            // Build/tag sync job
            Property<GitBranchConfigurationProperty> branchConfigurationProperty = propertyService.getProperty(branch, GitBranchConfigurationPropertyType.class);
            if (!branchConfigurationProperty.isEmpty() && branchConfigurationProperty.getValue().getBuildTagInterval() > 0) {
                jobs.add(createBuildSyncJob(branch, configuration));
            }
        });
        return jobs;
    }

    @Override
    public boolean isBranchConfiguredForGit(Branch branch) {
        return getBranchConfiguration(branch).isValid();
    }

    @Override
    public Ack launchBuildSync(ID branchId) {
        // Gets the branch
        Branch branch = structureService.getBranch(branchId);
        // Gets its configuration
        GitConfiguration configuration = getBranchConfiguration(branch);
        // If valid, launches a job
        if (configuration.isValid()) {
            return jobQueueService.queue(createBuildSyncJob(branch, configuration));
        }
        // Else, nothing has happened
        else {
            return Ack.NOK;
        }
    }

    @Override
    @Transactional
    public GitChangeLog changeLog(BuildDiffRequest request) {
        try (Transaction ignored = transactionService.start()) {
            Branch branch = structureService.getBranch(request.getBranch());
            GitConfiguration configuration = getBranchConfiguration(branch);
            // Forces Git sync before
            gitClientFactory.getClient(configuration).sync(logger::debug);
            // Change log computation
            return new GitChangeLog(
                    UUID.randomUUID().toString(),
                    branch,
                    configuration,
                    getSCMBuildView(request.getFrom()),
                    getSCMBuildView(request.getTo())
            );
        }
    }

    @Override
    public GitChangeLogCommits getChangeLogCommits(GitChangeLog changeLog) {
        // Gets the client client for this branch
        GitClient gitClient = gitClientFactory.getClient(changeLog.getScmBranch());
        // Gets the configuration
        GitConfiguration gitConfiguration = changeLog.getScmBranch();
        // Gets the build boundaries
        Build buildFrom = changeLog.getFrom().getBuild();
        Build buildTo = changeLog.getTo().getBuild();
        // Commit boundaries
        String commitFrom = gitConfiguration.getCommitFromBuild(buildFrom);
        String commitTo = gitConfiguration.getCommitFromBuild(buildTo);
        // Gets the commits
        GitLog log = gitClient.log(commitFrom, commitTo);
        List<GitCommit> commits = log.getCommits();
        List<GitUICommit> uiCommits = toUICommits(gitConfiguration, commits);
        return new GitChangeLogCommits(
                new GitUILog(
                        log.getPlot(),
                        uiCommits
                )
        );
    }

    @Override
    public GitChangeLogIssues getChangeLogIssues(GitChangeLog changeLog) {
        // Commits must have been loaded first
        if (changeLog.getCommits() == null) {
            changeLog.withCommits(getChangeLogCommits(changeLog));
        }
        // In a transaction
        try (Transaction ignored = transactionService.start()) {
            // Configuration
            GitConfiguration configuration = changeLog.getScmBranch();
            // Issue service
            ConfiguredIssueService configuredIssueService = issueServiceRegistry.getConfiguredIssueService(configuration.getIssueServiceConfigurationIdentifier());
            if (configuredIssueService == null) {
                throw new IssueServiceNotConfiguredException();
            }
            // Index of issues, sorted by keys
            Map<String, GitChangeLogIssue> issues = new TreeMap<>();
            // For all commits in this commit log
            for (GitUICommit gitUICommit : changeLog.getCommits().getLog().getCommits()) {
                Set<String> keys = configuredIssueService.extractIssueKeysFromMessage(gitUICommit.getCommit().getFullMessage());
                for (String key : keys) {
                    GitChangeLogIssue existingIssue = issues.get(key);
                    if (existingIssue != null) {
                        existingIssue.add(gitUICommit);
                    } else {
                        Issue issue = configuredIssueService.getIssue(key);
                        existingIssue = GitChangeLogIssue.of(issue, gitUICommit);
                        issues.put(key, existingIssue);
                    }
                }
            }
            // List of issues
            List<GitChangeLogIssue> issuesList = new ArrayList<>(issues.values());
            // Issues link
            IssueServiceConfigurationRepresentation issueServiceConfiguration = configuredIssueService.getIssueServiceConfigurationRepresentation();
            // OK
            return new GitChangeLogIssues(issueServiceConfiguration, issuesList);

        }
    }

    @Override
    public GitChangeLogFiles getChangeLogFiles(GitChangeLog changeLog) {
        // Gets the client client for this branch
        GitClient gitClient = gitClientFactory.getClient(changeLog.getScmBranch());
        // Gets the configuration
        GitConfiguration gitConfiguration = gitClient.getConfiguration();
        // Gets the build boundaries
        Build buildFrom = changeLog.getFrom().getBuild();
        Build buildTo = changeLog.getTo().getBuild();
        // Commit boundaries
        String commitFrom = gitConfiguration.getCommitFromBuild(buildFrom);
        String commitTo = gitConfiguration.getCommitFromBuild(buildTo);
        // Diff
        final GitDiff diff = gitClient.diff(commitFrom, commitTo);
        // File change links
        String fileChangeLinkFormat = gitConfiguration.getFileAtCommitLink();
        // OK
        return new GitChangeLogFiles(
                diff.getEntries().stream()
                        .map(entry -> toChangeLogFile(entry).withUrl(
                                getDiffUrl(diff, entry, fileChangeLinkFormat)
                        ))
                        .collect(Collectors.toList())
        );
    }

    @Override
    public boolean scanCommits(GitConfiguration configuration, Predicate<RevCommit> scanFunction) {
        // Gets the client client for this branch
        GitClient gitClient = gitClientFactory.getClient(configuration);
        // Scanning
        return gitClient.scanCommits(scanFunction);
    }

    @Override
    public OntrackGitIssueInfo getIssueInfo(ID branchId, String key) {
        Branch branch = structureService.getBranch(branchId);
        // Configuration
        GitConfiguration configuration = getBranchConfiguration(branch);
        if (!configuration.isValid()) {
            throw new GitBranchNotConfiguredException(branchId);
        }
        // Issue service
        ConfiguredIssueService configuredIssueService = issueServiceRegistry.getConfiguredIssueService(configuration.getIssueServiceConfigurationIdentifier());
        if (configuredIssueService == null) {
            throw new GitBranchIssueServiceNotConfiguredException(branchId);
        }
        // Gets the details about the issue
        Issue issue = configuredIssueService.getIssue(key);

        // Collects commits per branches
        List<OntrackGitIssueCommitInfo> commitInfos = collectIssueCommitInfos(key);

        // OK
        return new OntrackGitIssueInfo(
                configuration,
                configuredIssueService.getIssueServiceConfigurationRepresentation(),
                issue,
                commitInfos
        );
    }

    private List<OntrackGitIssueCommitInfo> collectIssueCommitInfos(String key) {
        // Index of commit infos
        Map<String, OntrackGitIssueCommitInfo> commitInfos = new LinkedHashMap<>();
        // For all configured branches
        forEachConfiguredBranch((branch, configuration) -> {
            // Gets the Git client for this branch
            GitClient gitClient = gitClientFactory.getClient(configuration);
            // Issue service
            ConfiguredIssueService configuredIssueService = issueServiceRegistry.getConfiguredIssueService(configuration.getIssueServiceConfigurationIdentifier());
            if (configuredIssueService != null) {
                // List of commits for this branch
                List<RevCommit> revCommits = new ArrayList<>();
                // Scanning this branch's repository for the commit
                gitClient.scanCommits(revCommit -> {
                    String message = revCommit.getFullMessage();
                    Set<String> keys = configuredIssueService.extractIssueKeysFromMessage(message);
                    if (configuredIssueService.containsIssueKey(key, keys)) {
                        // We have a commit for this branch!
                        revCommits.add(revCommit);
                    }
                    return false; // Scanning all commits
                });
                // If at least one commit
                if (revCommits.size() > 0) {
                    // Gets the last commit (which is the first in the list)
                    RevCommit revCommit = revCommits.get(0);
                    // Commit explained (independent from the branch)
                    GitCommit commit = gitClient.toCommit(revCommit);
                    String commitId = commit.getId();
                    // Gets any existing commit info
                    OntrackGitIssueCommitInfo commitInfo = commitInfos.get(commitId);
                    // If not defined, creates an entry
                    if (commitInfo == null) {
                        // UI commit (independent from the branch)
                        GitUICommit uiCommit = toUICommit(
                                configuration.getCommitLink(),
                                getMessageAnnotators(configuration),
                                commit
                        );
                        // Commit info
                        commitInfo = OntrackGitIssueCommitInfo.of(uiCommit);
                        // Indexation
                        commitInfos.put(commitId, commitInfo);
                    }
                    // Collects branch info
                    OntrackGitIssueCommitBranchInfo branchInfo = OntrackGitIssueCommitBranchInfo.of(branch);
                    // Gets the last build for this branch
                    Optional<Build> buildAfterCommit = getEarliestBuildAfterCommit(commitId, branch, configuration, gitClient);
                    if (buildAfterCommit.isPresent()) {
                        Build build = buildAfterCommit.get();
                        // Gets the build view
                        BuildView buildView = structureService.getBuildView(build);
                        // Adds it to the list
                        branchInfo = branchInfo.withBuildView(buildView);
                        // Collects the promotions for the branch
                        branchInfo = branchInfo.withBranchStatusView(
                                structureService.getEarliestPromotionsAfterBuild(build)
                        );
                    }
                    // Adds the info
                    commitInfo.add(branchInfo);
                }
            }
        });
        // OK
        return Lists.newArrayList(commitInfos.values());
    }

    @Override
    public Optional<GitUICommit> lookupCommit(GitConfiguration configuration, String id) {
        // Gets the client client for this configuration
        GitClient gitClient = gitClientFactory.getClient(configuration);
        // Gets the commit
        GitCommit gitCommit = gitClient.getCommitFor(id);
        if (gitCommit != null) {
            String commitLink = configuration.getCommitLink();
            List<? extends MessageAnnotator> messageAnnotators = getMessageAnnotators(configuration);
            return Optional.of(
                    toUICommit(
                            commitLink,
                            messageAnnotators,
                            gitCommit
                    )
            );
        } else {
            return Optional.empty();
        }
    }

    @Override
    public OntrackGitCommitInfo getCommitInfo(ID branchId, String commit) {
        /**
         * The information is actually collected on all branches.
         */
        return getOntrackGitCommitInfo(commit);
    }

    @Override
    public List<String> getRemoteBranches(GitConfiguration gitConfiguration) {
        return gitClientFactory.getClient(gitConfiguration).getRemoteBranches();
    }

    private OntrackGitCommitInfo getOntrackGitCommitInfo(String commit) {
        // Reference data
        AtomicReference<GitCommit> theCommit = new AtomicReference<>();
        AtomicReference<GitConfiguration> theConfiguration = new AtomicReference<>();
        // Data to collect
        Collection<BuildView> buildViews = new ArrayList<>();
        Collection<BranchStatusView> branchStatusViews = new ArrayList<>();
        // For all configured branches
        forEachConfiguredBranch((branch, configuration) -> {
            // Gets the client client for this branch
            GitClient gitClient = gitClientFactory.getClient(configuration);
            // Gets the commit for this repository
            GitCommit gitCommit = gitClient.getCommitFor(commit);
            if (gitCommit != null) {
                // Reference
                if (theCommit.get() == null) {
                    theCommit.set(gitCommit);
                    theConfiguration.set(configuration);
                }
                // Gets the earliest build on this branch that contains this commit
                getEarliestBuildAfterCommit(commit, branch, configuration, gitClient)
                        // ... and it present collect its data
                        .ifPresent(build -> {
                            // Gets the build view
                            BuildView buildView = structureService.getBuildView(build);
                            // Adds it to the list
                            buildViews.add(buildView);
                            // Collects the promotions for the branch
                            branchStatusViews.add(
                                    structureService.getEarliestPromotionsAfterBuild(build)
                            );
                        });
            }
        });

        // OK
        if (theCommit.get() != null)

        {
            String commitLink = theConfiguration.get().getCommitLink();
            List<? extends MessageAnnotator> messageAnnotators = getMessageAnnotators(theConfiguration.get());
            return new OntrackGitCommitInfo(
                    toUICommit(
                            commitLink,
                            messageAnnotators,
                            theCommit.get()
                    ),
                    buildViews,
                    branchStatusViews
            );
        } else

        {
            throw new GitCommitNotFoundException(commit);
        }

    }

    protected <T> Optional<Build> getEarliestBuildAfterCommit(String commit, Branch branch, GitConfiguration configuration, GitClient gitClient) {
        @SuppressWarnings("unchecked")
        ConfiguredBuildGitCommitLink<T> configuredBuildGitCommitLink = (ConfiguredBuildGitCommitLink<T>) configuration.getBuildCommitLink();
        // Delegates to the build commit link...
        return configuredBuildGitCommitLink.getLink()
                // ... by getting candidate references
                .getBuildCandidateReferences(commit, gitClient, configuredBuildGitCommitLink.getData())
                        // ... gets the builds
                .map(buildName -> structureService.findBuildByName(branch.getProject().getName(), branch.getName(), buildName))
                        // ... filter on existing builds
                .filter(Optional::isPresent).map(Optional::get)
                        // ... filter the builds using the link
                .filter(build -> configuredBuildGitCommitLink.getLink().isBuildEligible(build, configuredBuildGitCommitLink.getData()))
                        // ... sort by decreasing date
                .sorted((o1, o2) -> (o1.id() - o2.id()))
                        // ... takes the first build
                .findFirst();
    }

    private String getDiffUrl(GitDiff diff, GitDiffEntry entry, String fileChangeLinkFormat) {
        return fileChangeLinkFormat
                .replace("{commit}", entry.getReferenceId(diff.getFrom(), diff.getTo()))
                .replace("{path}", entry.getReferencePath());
    }

    private GitChangeLogFile toChangeLogFile(GitDiffEntry entry) {
        switch (entry.getChangeType()) {
            case ADD:
                return GitChangeLogFile.of(SCMChangeLogFileChangeType.ADDED, entry.getNewPath());
            case COPY:
                return GitChangeLogFile.of(SCMChangeLogFileChangeType.COPIED, entry.getOldPath(), entry.getNewPath());
            case DELETE:
                return GitChangeLogFile.of(SCMChangeLogFileChangeType.DELETED, entry.getOldPath());
            case MODIFY:
                return GitChangeLogFile.of(SCMChangeLogFileChangeType.MODIFIED, entry.getOldPath());
            case RENAME:
                return GitChangeLogFile.of(SCMChangeLogFileChangeType.RENAMED, entry.getOldPath(), entry.getNewPath());
            default:
                return GitChangeLogFile.of(SCMChangeLogFileChangeType.UNDEFINED, entry.getOldPath(), entry.getNewPath());
        }
    }

    private List<GitUICommit> toUICommits(GitConfiguration gitConfiguration, List<GitCommit> commits) {
        // Link?
        String commitLink = gitConfiguration.getCommitLink();
        // Issue-based annotations
        List<? extends MessageAnnotator> messageAnnotators = getMessageAnnotators(gitConfiguration);
        // OK
        return commits.stream()
                .map(commit -> toUICommit(commitLink, messageAnnotators, commit))
                .collect(Collectors.toList());
    }

    private GitUICommit toUICommit(String commitLink, List<? extends MessageAnnotator> messageAnnotators, GitCommit commit) {
        return new GitUICommit(
                commit,
                MessageAnnotationUtils.annotate(commit.getShortMessage(), messageAnnotators),
                MessageAnnotationUtils.annotate(commit.getFullMessage(), messageAnnotators),
                StringUtils.replace(commitLink, "{commit}", commit.getId())
        );
    }

    private List<? extends MessageAnnotator> getMessageAnnotators(GitConfiguration gitConfiguration) {
        List<? extends MessageAnnotator> messageAnnotators;
        String issueServiceConfigurationIdentifier = gitConfiguration.getIssueServiceConfigurationIdentifier();
        if (StringUtils.isNotBlank(issueServiceConfigurationIdentifier)) {
            ConfiguredIssueService configuredIssueService = issueServiceRegistry.getConfiguredIssueService(issueServiceConfigurationIdentifier);
            if (configuredIssueService != null) {
                // Gets the message annotator
                Optional<MessageAnnotator> messageAnnotator = configuredIssueService.getMessageAnnotator();
                // If present annotate the messages
                if (messageAnnotator.isPresent()) {
                    messageAnnotators = Collections.singletonList(messageAnnotator.get());
                } else {
                    messageAnnotators = Collections.emptyList();
                }
            } else {
                messageAnnotators = Collections.emptyList();
            }
        } else {
            messageAnnotators = Collections.emptyList();
        }
        return messageAnnotators;
    }

    private SCMBuildView<GitBuildInfo> getSCMBuildView(ID buildId) {
        return new SCMBuildView<>(getBuildView(buildId), new GitBuildInfo());
    }

    @Override
    public GitConfiguration getBranchConfiguration(Branch branch) {
        // Empty configuration
        GitConfiguration configuration = GitConfiguration.empty();
        // Configurators{
        for (GitConfigurator configurator : configurators) {
            configuration = configurator.configure(configuration, branch);
        }
        // Unique name
        if (StringUtils.isNotBlank(configuration.getRemote())) {
            configuration = configuration.withName(
                    format(
                            "%s/%s/%s",
                            branch.getProject().getName(),
                            branch.getName(),
                            configuration.getRemote()
                    )
            );
        }
        // OK
        return configuration;
    }

    private Job createBuildSyncJob(Branch branch, GitConfiguration configuration) {
        return new BranchJob(branch) {

            @Override
            public String getCategory() {
                return "GitBuildTagSync";
            }

            @Override
            public String getId() {
                return String.valueOf(branch.getId());
            }

            @Override
            public String getDescription() {
                return format(
                        "Git build/tag synchro for branch %s/%s",
                        branch.getProject().getName(),
                        branch.getName()
                );
            }

            @Override
            public int getInterval() {
                return configuration.getIndexationInterval();
            }

            @Override
            public JobTask createTask() {
                return new RunnableJobTask(info -> buildSync(branch, configuration, info));
            }
        };
    }

    private Job createIndexationJob(Branch branch, GitConfiguration config) {
        return new BranchJob(branch) {

            @Override
            public String getCategory() {
                return "GitIndexation";
            }

            @Override
            public String getId() {
                return config.getName();
            }

            @Override
            public String getDescription() {
                return format(
                        "Git indexation for %s",
                        config.getName()
                );
            }

            @Override
            public int getInterval() {
                return config.getIndexationInterval();
            }

            @Override
            public JobTask createTask() {
                return new RunnableJobTask(
                        info -> index(config, info)
                );
            }
        };
    }

    protected void buildSync(Branch branch, GitConfiguration configuration, JobInfoListener info) {
        info.post(format("Git build/tag sync for %s/%s", branch.getProject().getName(), branch.getName()));
        // Gets the branch Git client
        GitClient gitClient = gitClientFactory.getClient(configuration);
        // Configuration for the sync
        Property<GitBranchConfigurationProperty> confProperty = propertyService.getProperty(branch, GitBranchConfigurationPropertyType.class);
        boolean override = !confProperty.isEmpty() && confProperty.getValue().isOverride();
        // Makes sure of synchronization
        info.post("Synchronizing before importing");
        gitClient.sync(info::post);
        // Gets the list of tags
        info.post("Getting list of tags");
        Collection<GitTag> tags = gitClient.getTags();
        // Creates the builds
        info.post("Creating builds from tags");
        for (GitTag tag : tags) {
            String tagName = tag.getName();
            // Filters the tags according to the branch tag pattern
            if (configuration.isValidTagName(tagName)) {
                // Build name
                info.post(format("Creating build for tag %s", tagName));
                configuration.getBuildNameFromTagName(tagName).ifPresent(buildName -> {

                    info.post(format("Build %s from tag %s", buildName, tagName));
                    // Existing build?
                    boolean createBuild;
                    Optional<Build> build = structureService.findBuildByName(branch.getProject().getName(), branch.getName(), buildName);
                    if (build.isPresent()) {
                        if (override) {
                            // Deletes the build
                            info.post(format("Deleting existing build %s", buildName));
                            structureService.deleteBuild(build.get().getId());
                            createBuild = true;
                        } else {
                            // Keeps the build
                            info.post(format("Build %s already exists", buildName));
                            createBuild = false;
                        }
                    } else {
                        createBuild = true;
                    }
                    // Actual creation
                    if (createBuild) {
                        info.post(format("Creating build %s from tag %s", buildName, tagName));
                        structureService.newBuild(
                                Build.of(
                                        branch,
                                        new NameDescription(
                                                buildName,
                                                "Imported from Git tag " + tagName
                                        ),
                                        securityService.getCurrentSignature().withTime(
                                                tag.getTime()
                                        )
                                )
                        );
                    }
                });
            }
        }
    }

    private void index(GitConfiguration config, JobInfoListener info) {
        info.post(format("Git sync for %s", config.getName()));
        // Gets the client for this configuration
        GitClient client = gitClientFactory.getClient(config);
        // Launches the synchronisation
        client.sync(info::post);
    }

}
