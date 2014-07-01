package net.nemerosa.ontrack.extension.svn;

import net.nemerosa.ontrack.extension.api.SearchExtension;
import net.nemerosa.ontrack.extension.issues.IssueServiceRegistry;
import net.nemerosa.ontrack.extension.support.AbstractExtension;
import net.nemerosa.ontrack.extension.svn.model.SVNRepositoryIssue;
import net.nemerosa.ontrack.extension.svn.service.SVNConfigurationService;
import net.nemerosa.ontrack.extension.svn.service.SVNService;
import net.nemerosa.ontrack.model.security.SecurityService;
import net.nemerosa.ontrack.model.structure.SearchProvider;
import net.nemerosa.ontrack.model.structure.SearchResult;
import net.nemerosa.ontrack.ui.controller.URIBuilder;
import net.nemerosa.ontrack.ui.support.AbstractSearchProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SVNIssueSearchExtension extends AbstractExtension implements SearchExtension {

    private final URIBuilder uriBuilder;
    private final IssueServiceRegistry issueServiceRegistry;
    private final SVNConfigurationService configurationService;
    private final SVNService svnService;
    private final SecurityService securityService;

    @Autowired
    public SVNIssueSearchExtension(
            SVNExtensionFeature extensionFeature,
            URIBuilder uriBuilder,
            IssueServiceRegistry issueServiceRegistry,
            SVNConfigurationService configurationService,
            SVNService svnService,
            SecurityService securityService
    ) {
        super(extensionFeature);
        this.uriBuilder = uriBuilder;
        this.issueServiceRegistry = issueServiceRegistry;
        this.configurationService = configurationService;
        this.svnService = svnService;
        this.securityService = securityService;
    }

    @Override
    public SearchProvider getSearchProvider() {
        return new AbstractSearchProvider(uriBuilder) {

            @Override
            public boolean isTokenSearchable(String token) {
                return issueServiceRegistry.getIssueServices().stream()
                        .filter(s -> s.validIssueToken(token))
                        .findAny()
                        .isPresent();
            }

            @Override
            public Collection<SearchResult> search(String token) {
                return configurationService.getConfigurationDescriptors().stream()
                        .map(descriptor -> securityService.asAdmin(() -> svnService.getRepository(descriptor.getId())))
                        .map(repository -> svnService.searchIssues(repository, token))
                        .filter(Optional::isPresent).map(Optional::get)
                        .map(repositoryIssue -> new SearchResult(
                                repositoryIssue.getIssue().getKey(),
                                getSearchIssueDescription(repositoryIssue),
                                null, // FIXME Issue URI
                                null, // FIXME Issue view
                                100
                        ))
                        .collect(Collectors.toList());
            }
        };
    }

    // TODO The link to the repository+issue can link to several projects and branches


    private String getSearchIssueDescription(SVNRepositoryIssue repositoryIssue) {
        return String.format("Issue %s in %s repository: %s",
                repositoryIssue.getIssue().getKey(),
                repositoryIssue.getRepository().getConfiguration().getName(),
                repositoryIssue.getIssue().getSummary());
    }
}
