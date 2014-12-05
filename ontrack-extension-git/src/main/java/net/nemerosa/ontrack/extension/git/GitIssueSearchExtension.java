package net.nemerosa.ontrack.extension.git;

import lombok.Data;
import net.nemerosa.ontrack.extension.api.SearchExtension;
import net.nemerosa.ontrack.extension.git.client.impl.GitException;
import net.nemerosa.ontrack.extension.git.model.GitBranchConfiguration;
import net.nemerosa.ontrack.extension.git.model.GitConfiguration;
import net.nemerosa.ontrack.extension.git.service.GitService;
import net.nemerosa.ontrack.extension.issues.IssueServiceRegistry;
import net.nemerosa.ontrack.extension.issues.model.ConfiguredIssueService;
import net.nemerosa.ontrack.extension.issues.model.Issue;
import net.nemerosa.ontrack.extension.support.AbstractExtension;
import net.nemerosa.ontrack.model.structure.Branch;
import net.nemerosa.ontrack.model.structure.ID;
import net.nemerosa.ontrack.model.structure.SearchProvider;
import net.nemerosa.ontrack.model.structure.SearchResult;
import net.nemerosa.ontrack.ui.controller.URIBuilder;
import net.nemerosa.ontrack.ui.support.AbstractSearchProvider;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

@Component
public class GitIssueSearchExtension extends AbstractExtension implements SearchExtension {

    private final GitService gitService;
    private final IssueServiceRegistry issueServiceRegistry;
    private final URIBuilder uriBuilder;

    @Autowired
    public GitIssueSearchExtension(
            GitExtensionFeature extensionFeature,
            GitService gitService,
            IssueServiceRegistry issueServiceRegistry,
            URIBuilder uriBuilder) {
        super(extensionFeature);
        this.gitService = gitService;
        this.issueServiceRegistry = issueServiceRegistry;
        this.uriBuilder = uriBuilder;
    }

    @Override
    public SearchProvider getSearchProvider() {
        return new GitIssueSearchProvider();
    }

    @Data
    protected static class BranchSearchConfiguration {

        private final Branch branch;
        private final GitBranchConfiguration gitBranchConfiguration;
        private final ConfiguredIssueService configuredIssueService;

    }

    protected class GitIssueSearchProvider extends AbstractSearchProvider {

        private final Collection<BranchSearchConfiguration> branchSearchConfigurations;

        public GitIssueSearchProvider() {
            super(uriBuilder);
            branchSearchConfigurations = new ArrayList<>();
            gitService.forEachConfiguredBranch((branch, branchConfiguration) -> {
                GitConfiguration config = branchConfiguration.getConfiguration();
                String issueServiceConfigurationIdentifier = config.getIssueServiceConfigurationIdentifier();
                if (StringUtils.isNotBlank(issueServiceConfigurationIdentifier)) {
                    ConfiguredIssueService configuredIssueService = issueServiceRegistry.getConfiguredIssueService(issueServiceConfigurationIdentifier);
                    if (configuredIssueService != null) {
                        branchSearchConfigurations.add(new BranchSearchConfiguration(
                                branch,
                                branchConfiguration,
                                configuredIssueService
                        ));
                    }
                }
            });
        }

        @Override
        public boolean isTokenSearchable(String token) {
            return branchSearchConfigurations.stream()
                    .filter(c -> c.getConfiguredIssueService().getIssueServiceExtension().validIssueToken(token))
                    .findAny()
                    .isPresent();
        }

        @Override
        public Collection<SearchResult> search(String token) {
            // Map of results per project, with the first result being the one for the first corresponding branch
            Map<ID, SearchResult> projectResults = new LinkedHashMap<>();
            // For all the configurations
            for (BranchSearchConfiguration c : branchSearchConfigurations) {
                ID projectId = c.getBranch().getProjectId();
                // Skipping if associated project is already associated with the issue
                if (!projectResults.containsKey(projectId)) {
                    // ... searches for the issue token in the git repository
                    boolean found;
                    try {
                        found = gitService.scanCommits(c.getGitBranchConfiguration(), commit -> scanIssue(c, commit, token));
                    } catch (GitException ignored) {
                        // Silent failure in case of problems with the Git repository
                        found = false;
                    }
                    // ... and if found
                    if (found) {
                        // ... loads the issue
                        Issue issue = c.getConfiguredIssueService().getIssue(token);
                        // Saves the result for the project
                        projectResults.put(
                                projectId,
                                new SearchResult(
                                        issue.getDisplayKey(),
                                        String.format("Issue %s found in project %s",
                                                issue.getKey(),
                                                c.getBranch().getProject().getName()
                                        ),
                                        uri(on(GitController.class).issueInfo(
                                                c.getBranch().getId(),
                                                issue.getKey()
                                        )),
                                        String.format("extension/git/%d/issue/%s",
                                                c.getBranch().id(),
                                                issue.getKey()),
                                        100
                                )
                        );
                    }
                }
            }
            // OK
            return projectResults.values();
        }

        private boolean scanIssue(BranchSearchConfiguration c, RevCommit commit, String key) {
            String message = commit.getFullMessage();
            Set<String> keys = c.getConfiguredIssueService().extractIssueKeysFromMessage(message);
            return c.getConfiguredIssueService().containsIssueKey(key, keys);
        }
    }
}
