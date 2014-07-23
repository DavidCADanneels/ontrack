package net.nemerosa.ontrack.extension.git.model;

import lombok.Data;
import net.nemerosa.ontrack.extension.issues.model.IssueServiceConfigurationRepresentation;
import net.nemerosa.ontrack.extension.support.configurations.UserPasswordConfiguration;
import net.nemerosa.ontrack.model.form.*;
import net.nemerosa.ontrack.model.support.ConfigurationDescriptor;

import java.util.List;

import static net.nemerosa.ontrack.model.form.Form.defaultNameField;

@Data
public class GitConfiguration implements UserPasswordConfiguration<GitConfiguration> {

    /**
     * Name of this configuration
     */
    private final String name;

    /**
     * Remote path to the source repository
     */
    private final String remote;

    /**
     * User name
     */
    private final String user;

    /**
     * User password
     */
    private final String password;

    /**
     * Link to a commit, using {commit} as placeholder
     */
    private final String commitLink;

    /**
     * Link to a file at a given commit, using {commit} and {path} as placeholders
     */
    private final String fileAtCommitLink;

    /**
     * Indexation interval
     */
    private final int indexationInterval;

    /**
     * ID to the {@link net.nemerosa.ontrack.extension.issues.model.IssueServiceConfiguration} associated
     * with this repository.
     */
    private final String issueServiceConfigurationIdentifier;

    @Override
    public ConfigurationDescriptor getDescriptor() {
        return new ConfigurationDescriptor(
                name,
                String.format("%s (%s)", name, remote)
        );
    }

    @Override
    public GitConfiguration obfuscate() {
        return this;
    }

    @Override
    public GitConfiguration withPassword(String password) {
        return new GitConfiguration(
                name,
                remote,
                user,
                password,
                commitLink,
                fileAtCommitLink,
                indexationInterval,
                issueServiceConfigurationIdentifier
        );
    }

    public static Form form(List<IssueServiceConfigurationRepresentation> availableIssueServiceConfigurations) {
        return Form.create()
                .with(defaultNameField())
                .with(
                        Text.of("remote")
                                .label("Remote")
                                .help("Remote path to the source repository")
                )
                .with(
                        Text.of("user")
                                .label("User")
                                .length(16)
                                .optional()
                )
                .with(
                        Password.of("password")
                                .label("Password")
                                .length(40)
                                .optional()
                )
                .with(
                        Text.of("commitLink")
                                .label("Commit link")
                                .length(250)
                                .optional()
                                .help("Link to a commit, using {commit} as placeholder")
                )
                .with(
                        Text.of("fileAtCommitLink")
                                .label("File at commit link")
                                .length(250)
                                .optional()
                                .help("Link to a file at a given commit, using {commit} and {path} as placeholders")
                )
                .with(
                        Int.of("indexationInterval")
                                .label("Indexation interval")
                                .min(0)
                                .max(60 * 24)
                                .value(0)
                                .help("Interval (in minutes) between each indexation of the Git repository. A " +
                                        "zero value indicates that no indexation must take place automatically and they " +
                                        "have to be triggered manually.")
                )
                .with(
                        Selection.of("issueServiceConfigurationIdentifier")
                                .label("Issue configuration")
                                .help("Select an issue service that is sued to associate tickets and issues to the source.")
                                .optional()
                                .items(availableIssueServiceConfigurations)
                );
    }

    public Form asForm(List<IssueServiceConfigurationRepresentation> availableIssueServiceConfigurations) {
        return form(availableIssueServiceConfigurations)
                .with(defaultNameField().readOnly().value(name))
                .fill("remote", remote)
                .fill("user", user)
                .fill("password", "")
                .fill("commitLink", commitLink)
                .fill("fileAtCommitLink", fileAtCommitLink)
                .fill("indexationInterval", indexationInterval)
                .fill("issueServiceConfigurationIdentifier", issueServiceConfigurationIdentifier)
                ;
    }
}
