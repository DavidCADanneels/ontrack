package net.nemerosa.ontrack.extension.svn.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Wither;
import net.nemerosa.ontrack.extension.issues.model.IssueServiceConfigurationRepresentation;
import net.nemerosa.ontrack.model.support.UserPasswordConfiguration;
import net.nemerosa.ontrack.model.form.*;
import net.nemerosa.ontrack.model.support.ConfigurationDescriptor;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.function.Function;

import static net.nemerosa.ontrack.model.form.Form.defaultNameField;

@Data
@AllArgsConstructor
public class SVNConfiguration implements UserPasswordConfiguration<SVNConfiguration> {

    private final String name;
    private final String url;
    @Wither
    private final String user;
    @SuppressWarnings("UnusedDeclaration")
    private final String password;
    @Wither
    private final String tagFilterPattern;
    @Wither
    private final String browserForPath;
    @Wither
    private final String browserForRevision;
    @Wither
    private final String browserForChange;
    @Wither
    private final int indexationInterval;
    @Wither
    private final long indexationStart;
    @Wither
    private final String issueServiceConfigurationIdentifier;

    public static SVNConfiguration of(String name, String url) {
        return new SVNConfiguration(
                name,
                url,
                null, null, // user, password
                "",         // tag filter pattern
                "", "", "", // browser URL
                0, 1L,      // indexation
                null        // issue service
        );
    }

    public static Form form(List<IssueServiceConfigurationRepresentation> availableIssueServiceConfigurations) {
        return Form.create()
                .with(defaultNameField())
                .with(
                        // Note that the URL property cannot be implemented through a URL field
                        // since some SVN repository URL could use the svn: protocol or other.
                        Text.of("url")
                                .label("URL")
                                .help("URL to the root of a SVN repository")
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
                        Text.of("tagFilterPattern")
                                .label("Tag filter pattern")
                                .length(100)
                                .optional()
                                .help("Regular expression applied to tag names. Any tag whose name matches " +
                                        "will be excluded from the tags. By default, no tag is excluded.")
                )
                .with(
                        Text.of("browserForPath")
                                .label("Browsing URL for a path")
                                .length(400)
                                .optional()
                                .help("URL that defines how to browse to a path. The path is relative to the " +
                                        "repository root and must be parameterized as {path} in the URL.")
                )
                .with(
                        Text.of("browserForRevision")
                                .label("Browsing URL for a revision")
                                .length(400)
                                .optional()
                                .help("URL that defines how to browse to a revision. The revision must be " +
                                        "parameterized as {revision} in the URL.")
                )
                .with(
                        Text.of("browserForChange")
                                .label("Browsing URL for a change")
                                .length(400)
                                .optional()
                                .help("URL that defines how to browse to the changes of a path at a given revision. " +
                                        "The revision must be parameterized as {revision} in the URL and the path " +
                                        "as {path}.")
                )
                .with(
                        Int.of("indexationInterval")
                                .label("Indexation interval")
                                .min(0)
                                .max(60 * 24)
                                .value(0)
                                .help("Interval (in minutes) between each indexation of the Subversion repository. A " +
                                        "zero value indicates that no indexation must take place automatically and they " +
                                        "have to be triggered manually.")
                )
                .with(
                        Int.of("indexationStart")
                                .label("Indexation start")
                                .min(1)
                                .value(1)
                                .help("Revision to start the indexation from.")
                )
                .with(
                        Selection.of("issueServiceConfigurationIdentifier")
                                .label("Issue configuration")
                                .help("Select an issue service that is sued to associate tickets and issues to the source.")
                                .optional()
                                .items(availableIssueServiceConfigurations)
                );
    }

    @Override
    public SVNConfiguration obfuscate() {
        return new SVNConfiguration(
                name,
                url,
                user,
                "",
                tagFilterPattern,
                browserForPath,
                browserForRevision,
                browserForChange,
                indexationInterval,
                indexationStart,
                issueServiceConfigurationIdentifier
        );
    }

    public Form asForm(List<IssueServiceConfigurationRepresentation> availableIssueServiceConfigurations) {
        return form(availableIssueServiceConfigurations)
                .with(defaultNameField().readOnly().value(name))
                .fill("url", url)
                .fill("user", user)
                .fill("password", "")
                .fill("tagFilterPattern", tagFilterPattern)
                .fill("browserForPath", browserForPath)
                .fill("browserForRevision", browserForRevision)
                .fill("browserForChange", browserForChange)
                .fill("indexationInterval", indexationInterval)
                .fill("indexationStart", indexationStart)
                .fill("issueServiceConfigurationIdentifier", issueServiceConfigurationIdentifier)
                ;
    }

    @Override
    public SVNConfiguration withPassword(String password) {
        return new SVNConfiguration(
                name,
                url,
                user,
                password,
                tagFilterPattern,
                browserForPath,
                browserForRevision,
                browserForChange,
                indexationInterval,
                indexationStart,
                issueServiceConfigurationIdentifier
        );
    }

    @Override
    public ConfigurationDescriptor getDescriptor() {
        return new ConfigurationDescriptor(name, name);
    }

    /**
     * Gets the absolute URL to a path relative to this repository.
     */
    public String getUrl(String path) {
        return StringUtils.stripEnd(url, "/")
                + "/"
                + StringUtils.stripStart(path, "/");
    }

    public String getRevisionBrowsingURL(long revision) {
        if (StringUtils.isNotBlank(browserForRevision)) {
            return browserForRevision.replace("{revision}", String.valueOf(revision));
        } else {
            return String.valueOf(revision);
        }
    }

    public String getPathBrowsingURL(String path) {
        if (StringUtils.isNotBlank(browserForPath)) {
            return browserForPath.replace("{path}", path);
        } else {
            return path;
        }
    }

    public String getFileChangeBrowsingURL(String path, long revision) {
        if (StringUtils.isNotBlank(browserForChange)) {
            return browserForChange.replace("{path}", path).replace("{revision}", String.valueOf(revision));
        } else {
            return path;
        }
    }

    @Override
    public SVNConfiguration clone(String targetConfigurationName, Function<String, String> replacementFunction) {
        return new SVNConfiguration(
                targetConfigurationName,
                replacementFunction.apply(url),
                replacementFunction.apply(user),
                password,
                replacementFunction.apply(tagFilterPattern),
                replacementFunction.apply(browserForPath),
                replacementFunction.apply(browserForRevision),
                replacementFunction.apply(browserForChange),
                indexationInterval,
                indexationStart,
                issueServiceConfigurationIdentifier
        );
    }
}
