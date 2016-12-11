package net.nemerosa.ontrack.extension.gitlab.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import net.nemerosa.ontrack.model.form.Form;
import net.nemerosa.ontrack.model.form.Password;
import net.nemerosa.ontrack.model.form.Text;
import net.nemerosa.ontrack.model.support.ConfigurationDescriptor;
import net.nemerosa.ontrack.model.support.UserPassword;
import net.nemerosa.ontrack.model.support.UserPasswordConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.beans.ConstructorProperties;
import java.util.Optional;
import java.util.function.Function;

import static java.lang.String.format;
import static net.nemerosa.ontrack.model.form.Form.defaultNameField;

/**
 * Configuration for accessing a GitLab application.
 */
@Data
public class GitLabConfiguration implements UserPasswordConfiguration<GitLabConfiguration> {

    /**
     * Name of this configuration
     */
    private final String name;

    /**
     * End point
     */
    private final String url;

    /**
     * User name
     */
    private final String user;

    /**
     * User password
     */
    private final String password;

    /**
     * Personal Access Token
     * <p>
     * TODO Defines how this can be used
     */
    private final String personalAccessToken;

    @ConstructorProperties({"name", "url", "user", "password", "personalAccessToken"})
    public GitLabConfiguration(String name, String url, String user, String password, String personalAccessToken) {
        this.name = name;
        this.url = url;
        this.user = user;
        this.password = password;
        this.personalAccessToken = personalAccessToken;
    }

    @Override
    @JsonIgnore
    public ConfigurationDescriptor getDescriptor() {
        return new ConfigurationDescriptor(
                name,
                format("%s (%s)", name, url)
        );
    }

    @Override
    public GitLabConfiguration obfuscate() {
        return this;
    }

    @Override
    public GitLabConfiguration withPassword(String password) {
        return new GitLabConfiguration(
                name,
                url,
                user,
                password,
                personalAccessToken
        );
    }

    public static Form form() {
        return Form.create()
                .with(defaultNameField())
                .with(
                        Text.of("url")
                                .label("URL")
                                .length(250)
                                .help("URL of the GitLab engine.")
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
                        Text.of("personalAccessToken")
                                .label("Personal Access Token")
                                .length(50)
                                .optional()
                );
    }

    public Form asForm() {
        return form()
                .with(defaultNameField().readOnly().value(name))
                .fill("url", url)
                .fill("user", user)
                .fill("password", "")
                .fill("personalAccessToken", personalAccessToken)
                ;
    }

    @Override
    public GitLabConfiguration clone(String targetConfigurationName, Function<String, String> replacementFunction) {
        return new GitLabConfiguration(
                targetConfigurationName,
                replacementFunction.apply(url),
                replacementFunction.apply(user),
                password,
                personalAccessToken
        );
    }

    @Override
    @JsonIgnore
    public Optional<UserPassword> getCredentials() {
        if (StringUtils.isNotBlank(personalAccessToken)) {
            return Optional.of(
                    new UserPassword(
                            personalAccessToken,
                            "PRIVATE-TOKEN"
                    )
            );
        } else if (StringUtils.isNotBlank(user)) {
            return Optional.of(
                    new UserPassword(
                            user,
                            password
                    )
            );
        } else {
            return Optional.empty();
        }
    }

}