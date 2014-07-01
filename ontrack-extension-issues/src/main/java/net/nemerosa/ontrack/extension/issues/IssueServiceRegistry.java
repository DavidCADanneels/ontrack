package net.nemerosa.ontrack.extension.issues;

import net.nemerosa.ontrack.extension.issues.model.ConfiguredIssueService;
import net.nemerosa.ontrack.extension.issues.model.IssueServiceConfiguration;
import net.nemerosa.ontrack.extension.issues.model.IssueServiceConfigurationRepresentation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IssueServiceRegistry {

    /**
     * Gets all the issue services
     */
    Collection<IssueServiceExtension> getIssueServices();

    /**
     * Gets an issue service by its ID
     */
    IssueServiceExtension getIssueService(String id);

    /**
     * Gets an issue service by its ID. It may be present or not.
     */
    Optional<IssueServiceExtension> getOptionalIssueService(String id);

    List<IssueServiceConfigurationRepresentation> getAvailableIssueServiceConfigurations();

    /**
     * Gets the issue service configuration for a given ID.
     *
     * @param id Compound id (service // configuration)
     * @return Configuration
     * @see net.nemerosa.ontrack.extension.issues.model.IssueServiceConfigurationRepresentation
     */
    IssueServiceConfiguration getIssueServiceConfigurationById(String id);

    /**
     * Gets the association between a service and a configuration, or <code>null</code>
     * if neither service nor configuration can be found.
     */
    ConfiguredIssueService getConfiguredIssueService(String issueServiceConfigurationIdentifier);
}
