package net.nemerosa.ontrack.extension.git.property;

import com.fasterxml.jackson.databind.JsonNode;
import net.nemerosa.ontrack.extension.support.AbstractPropertyType;
import net.nemerosa.ontrack.json.JsonUtils;
import net.nemerosa.ontrack.model.form.Form;
import net.nemerosa.ontrack.model.form.Text;
import net.nemerosa.ontrack.model.form.YesNo;
import net.nemerosa.ontrack.model.security.ProjectConfig;
import net.nemerosa.ontrack.model.security.SecurityService;
import net.nemerosa.ontrack.model.structure.ProjectEntity;
import net.nemerosa.ontrack.model.structure.ProjectEntityType;

import java.util.EnumSet;
import java.util.Set;

public class GitBranchConfigurationPropertyType extends AbstractPropertyType<GitBranchConfigurationProperty> {

    @Override
    public String getName() {
        return "Git branch";
    }

    @Override
    public String getDescription() {
        return "Git branch";
    }

    @Override
    public Set<ProjectEntityType> getSupportedEntityTypes() {
        return EnumSet.of(ProjectEntityType.BRANCH);
    }

    @Override
    public boolean canEdit(ProjectEntity entity, SecurityService securityService) {
        return securityService.isProjectFunctionGranted(entity.projectId(), ProjectConfig.class);
    }

    @Override
    public boolean canView(ProjectEntity entity, SecurityService securityService) {
        return true;
    }

    @Override
    public Form getEditionForm(GitBranchConfigurationProperty value) {
        return Form.create()
                .with(
                        Text.of("branch")
                                .label("Git branch")
                                .value(value != null ? value.getBranch() : "master")
                )
                .with(
                        Text.of("tagPattern")
                                .label("Tag pattern")
                                .help("Expression that describes the tags to import when " +
                                        "build/tag synchronisation is enabled. The * placeholder is used " +
                                        "to designate the placeholder for the build name. It defaults to *, " +
                                        "meaning that we have a 1:1 relationship between the tag and the build name.")
                                .value(value != null ? value.getTagPattern() : "*")
                )
                .with(
                        YesNo.of("override")
                                .label("Override builds")
                                .help("Can the existing builds be overridden by a synchronisation? If yes, " +
                                        "the existing validation and promotion runs would be lost as well.")
                                .value(value != null && value.isOverride())
                );
    }

    @Override
    public GitBranchConfigurationProperty fromClient(JsonNode node) {
        return fromStorage(node);
    }

    @Override
    public GitBranchConfigurationProperty fromStorage(JsonNode node) {
        return new GitBranchConfigurationProperty(
                JsonUtils.get(node, "branch", "master"),
                JsonUtils.get(node, "tagPattern", "*"),
                JsonUtils.getBoolean(node, "override", false)
        );
    }

    @Override
    public String getSearchKey(GitBranchConfigurationProperty value) {
        return value.getBranch();
    }
}
