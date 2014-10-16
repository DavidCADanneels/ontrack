package net.nemerosa.ontrack.extension.git;

import net.nemerosa.ontrack.extension.api.ExtensionManager;
import net.nemerosa.ontrack.extension.git.model.GitConfiguration;
import net.nemerosa.ontrack.extension.git.service.GitService;
import net.nemerosa.ontrack.model.form.Form;
import net.nemerosa.ontrack.model.form.Memo;
import net.nemerosa.ontrack.model.structure.Branch;
import net.nemerosa.ontrack.model.support.AbstractTemplateSynchronisationSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class GitBranchesTemplateSynchronisationSource extends AbstractTemplateSynchronisationSource<GitBranchesTemplateSynchronisationSourceConfig> {

    private final GitExtensionFeature gitExtensionFeature;
    private final ExtensionManager extensionManager;
    private final GitService gitService;

    @Autowired
    public GitBranchesTemplateSynchronisationSource(GitExtensionFeature gitExtensionFeature, ExtensionManager extensionManager, GitService gitService) {
        super(GitBranchesTemplateSynchronisationSourceConfig.class);
        this.gitExtensionFeature = gitExtensionFeature;
        this.extensionManager = extensionManager;
        this.gitService = gitService;
    }

    @Override
    public String getId() {
        return "git-branches";
    }

    @Override
    public String getName() {
        return "Git branches";
    }

    @Override
    public boolean isApplicable(Branch branch) {
        return extensionManager.isExtensionFeatureEnabled(gitExtensionFeature)
                && gitService.isBranchConfiguredForGit(branch);
    }

    @Override
    public Form getForm(Branch branch) {
        return Form.create()
                .with(
                        Memo.of("includes")
                                .label("Includes")
                                .optional()
                                .help("List of branches to include - one pattern per line, where " +
                                        "* can be used as a wildcard.")
                )
                .with(
                        Memo.of("excludes")
                                .label("Excludes")
                                .optional()
                                .help("List of branches to exclude - one pattern per line, where " +
                                        "* can be used as a wildcard.")
                )
                ;
    }

    @Override
    public List<String> getBranchNames(Branch branch, GitBranchesTemplateSynchronisationSourceConfig config) {
        // Gets the Git configuration
        GitConfiguration gitConfiguration = gitService.getBranchConfiguration(branch);
        // Inclusion predicate
        Predicate<String> filter = config.getFilter();
        // TODO Transformation of characters in branch name in order to it to be valid
        // Gets the list of branches
        return gitService.getRemoteBranches(gitConfiguration).stream()
                .filter(filter)
                .sorted()
                .collect(Collectors.toList());
    }

}
