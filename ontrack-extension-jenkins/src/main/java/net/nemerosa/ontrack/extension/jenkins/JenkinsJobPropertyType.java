package net.nemerosa.ontrack.extension.jenkins;

import com.fasterxml.jackson.databind.JsonNode;
import net.nemerosa.ontrack.model.form.Form;
import net.nemerosa.ontrack.model.security.SecurityService;
import net.nemerosa.ontrack.model.structure.*;

import java.util.Optional;

public class JenkinsJobPropertyType extends AbstractJenkinsPropertyType<JenkinsJobProperty> {

    @Override
    public String getName() {
        return "Jenkins Job";
    }

    @Override
    public String getDescription() {
        return "Link to a Jenkins Job";
    }

    @Override
    public String getIconPath() {
        return "assets/extension/jenkins/JenkinsJob.png";
    }

    @Override
    public String getShortTemplatePath() {
        return "app/extension/jenkins/jenkins-job-property-short.html";
    }

    @Override
    public String getFullTemplatePath() {
        return "app/extension/jenkins/jenkins-job-property-full.html";
    }

    @Override
    public boolean applies(Class<? extends Entity> entityClass) {
        return entityClass.isAssignableFrom(Project.class)
                || entityClass.isAssignableFrom(Branch.class)
                || entityClass.isAssignableFrom(ValidationStamp.class);
    }

    @Override
    public boolean canEdit(Entity entity, SecurityService securityService) {
        // FIXME Needs access to the project for the authorizations
        return false;
    }

    @Override
    public boolean canView(Entity entity, SecurityService securityService) {
        // FIXME Needs access to the project for the authorizations
        return false;
    }

    @Override
    public Form getEditionForm(Optional<JenkinsJobProperty> value) {
        // FIXME Method net.nemerosa.ontrack.extension.jenkins.JenkinsJobPropertyType.getEditionForm
        return null;
    }

    @Override
    protected void validate(JenkinsJobProperty value) {
        // FIXME Method net.nemerosa.ontrack.extension.jenkins.JenkinsJobPropertyType.validate

    }

    @Override
    public JsonNode forStorage(JenkinsJobProperty value) {
        // FIXME Method net.nemerosa.ontrack.extension.jenkins.JenkinsJobPropertyType.forStorage
        return null;
    }

    @Override
    public JenkinsJobProperty fromStorage(JsonNode node) {
        // FIXME Method net.nemerosa.ontrack.extension.jenkins.JenkinsJobPropertyType.fromStorage
        return null;
    }
}
