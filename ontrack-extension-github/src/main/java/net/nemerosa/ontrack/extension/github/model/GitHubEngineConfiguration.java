package net.nemerosa.ontrack.extension.github.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import net.nemerosa.ontrack.extension.support.UserPasswordConfiguration;
import net.nemerosa.ontrack.model.form.Form;
import net.nemerosa.ontrack.model.form.Int;
import net.nemerosa.ontrack.model.form.Password;
import net.nemerosa.ontrack.model.form.Text;
import net.nemerosa.ontrack.model.support.ConfigurationDescriptor;
import net.nemerosa.ontrack.model.support.UserPassword;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.function.Function;

import static java.lang.String.format;
import static net.nemerosa.ontrack.model.form.Form.defaultNameField;

/**
 * Configuration for accessing a GitHub engine, github.com or GitHub enterprise.
 */
@Data
public class GitHubEngineConfiguration implements UserPasswordConfiguration<GitHubEngineConfiguration> {

    /**
     * github.com end point.
     */
    public static final String GITHUB_COM = "https://github.com";

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
     * OAuth2 token
     */
    private final String oauth2Token;

    @Override
    @JsonIgnore
    public ConfigurationDescriptor getDescriptor() {
        return new ConfigurationDescriptor(
                name,
                format("%s (%s)", name, url)
        );
    }

    @Override
    public GitHubEngineConfiguration obfuscate() {
        return this;
    }

    @Override
    public GitHubEngineConfiguration withPassword(String password) {
        return new GitHubEngineConfiguration(
                name,
                url,
                user,
                password,
                oauth2Token
        );
    }

    public static Form form() {
        return Form.create()
                .with(defaultNameField())
                .with(
                        Text.of("url")
                                .label("URL")
                                .length(250)
                                .optional()
                                .help(format("URL of the GitHub engine. Defaults to %s if not defined.", GITHUB_COM))
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
                        Text.of("oauth2Token")
                                .label("OAuth2 token")
                                .length(50)
                                .optional()
                )
                .with(
                        Int.of("indexationInterval")
                                .label("Indexation interval")
                                .min(0)
                                .max(60 * 24)
                                .value(0)
                                .help("@file:extension/github/help.net.nemerosa.ontrack.extension.github.model.GitHubConfiguration.indexationInterval.tpl.html")
                );
    }

    public Form asForm() {
        return form()
                .with(defaultNameField().readOnly().value(name))
                .fill("url", url)
                .fill("user", user)
                .fill("password", "")
                .fill("oauth2Token", oauth2Token)
                ;
    }

    @Override
    public GitHubEngineConfiguration clone(String targetConfigurationName, Function<String, String> replacementFunction) {
        return new GitHubEngineConfiguration(
                targetConfigurationName,
                replacementFunction.apply(url),
                replacementFunction.apply(user),
                password,
                oauth2Token
        );
    }

    @Override
    @JsonIgnore
    public Optional<UserPassword> getCredentials() {
        if (StringUtils.isNotBlank(oauth2Token)) {
            return Optional.of(
                    new UserPassword(
                            oauth2Token,
                            "x-oauth-basic"
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