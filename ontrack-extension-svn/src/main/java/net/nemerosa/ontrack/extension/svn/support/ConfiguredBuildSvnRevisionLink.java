package net.nemerosa.ontrack.extension.svn.support;

import lombok.Data;
import net.nemerosa.ontrack.extension.svn.model.BuildSvnRevisionLink;
import net.nemerosa.ontrack.extension.svn.model.IndexableBuildSvnRevisionLink;
import net.nemerosa.ontrack.extension.svn.model.SVNLocation;
import net.nemerosa.ontrack.extension.svn.property.SVNBranchConfigurationProperty;
import net.nemerosa.ontrack.model.structure.Branch;
import net.nemerosa.ontrack.model.structure.Build;
import net.nemerosa.ontrack.model.structure.ServiceConfiguration;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Function;

/**
 * Configured {@link net.nemerosa.ontrack.extension.svn.model.BuildSvnRevisionLink}.
 */
@Data
public class ConfiguredBuildSvnRevisionLink<T> {

    private final BuildSvnRevisionLink<T> link;
    private final T data;

    public ConfiguredBuildSvnRevisionLink<T> clone(Function<String, String> replacementFunction) {
        return new ConfiguredBuildSvnRevisionLink<>(
                link,
                link.clone(data, replacementFunction)
        );
    }

    public ServiceConfiguration toServiceConfiguration() {
        return new ServiceConfiguration(
                link.getId(),
                link.toJson(data)
        );
    }

    public boolean isValidBuildName(String name) {
        return link.isValidBuildName(data, name);
    }

    public OptionalLong getRevision(Build build, SVNBranchConfigurationProperty branchConfigurationProperty) {
        return link.getRevision(data, build, branchConfigurationProperty);
    }

    public String getBuildPath(Build build, SVNBranchConfigurationProperty branchConfigurationProperty) {
        return link.getBuildPath(data, build, branchConfigurationProperty);
    }

    public Optional<Build> getEarliestBuild(Branch branch, SVNLocation location, SVNLocation firstCopy, SVNBranchConfigurationProperty branchConfigurationProperty) {
        return link.getEarliestBuild(data, branch, location, firstCopy, branchConfigurationProperty);
    }

    public Optional<String> getBuildNameFromTagName(String tagName) {
        if (link instanceof IndexableBuildSvnRevisionLink) {
            return ((IndexableBuildSvnRevisionLink<T>) link).getBuildNameFromTagName(data, tagName);
        } else {
            throw new UnsupportedOperationException("getBuildNameFromPath is not supported for non indexable links");
        }
    }
}
