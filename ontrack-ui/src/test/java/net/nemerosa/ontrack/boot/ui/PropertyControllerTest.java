package net.nemerosa.ontrack.boot.ui;

import net.nemerosa.ontrack.extension.jenkins.JenkinsConfiguration;
import net.nemerosa.ontrack.extension.jenkins.JenkinsController;
import net.nemerosa.ontrack.extension.jenkins.JenkinsJobProperty;
import net.nemerosa.ontrack.extension.jenkins.JenkinsJobPropertyType;
import net.nemerosa.ontrack.model.form.Field;
import net.nemerosa.ontrack.model.form.Form;
import net.nemerosa.ontrack.model.security.GlobalSettings;
import net.nemerosa.ontrack.model.security.ProjectConfig;
import net.nemerosa.ontrack.model.security.ProjectCreation;
import net.nemerosa.ontrack.model.security.ProjectView;
import net.nemerosa.ontrack.model.structure.*;
import net.nemerosa.ontrack.ui.resource.Resource;
import net.nemerosa.ontrack.ui.resource.Resources;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static net.nemerosa.ontrack.json.JsonUtils.object;
import static net.nemerosa.ontrack.test.TestUtils.uid;
import static org.junit.Assert.*;

/**
 * Integration tests for the property controller.
 */
public class PropertyControllerTest extends AbstractWebTestSupport {

    @Autowired
    private PropertyController controller;

    @Autowired
    private JenkinsController jenkinsController;

    @Autowired
    private StructureService structureService;

    /**
     * List of editable properties for a project.
     */
    @Test
    public void project_properties_with_edition_allowed() throws Exception {
        Project project = asUser().with(ProjectCreation.class).call(() -> structureService.newProject(
                Project.of(nameDescription())
        ));
        Entity.isEntityDefined(project, "Project is defined");
        // Gets the properties for this project
        Resources<Resource<Property<?>>> properties = asUser().with(project.id(), ProjectConfig.class).call(() ->
                        controller.getProperties(ProjectEntityType.PROJECT, project.getId())
        );
        // Checks there is at least the Jenkins Job property
        assertNotNull("Properties should not be null", properties);
        Optional<Property<?>> property = properties.getResources().stream()
                .map(Resource::getData)
                .filter(p -> "Jenkins Job".equals(p.getType().getName()))
                .findFirst();
        assertTrue("At least the Jenkins Job property should have been found", property.isPresent());
        assertTrue("The Jenkins Job property should be editable", property.get().isEditable());
    }

    /**
     * List of editable properties for a project, filter by authorization.
     */
    @Test
    public void project_properties_filtered_by_authorization() throws Exception {
        Project project = asUser().with(ProjectCreation.class).call(() -> structureService.newProject(
                Project.of(nameDescription())
        ));
        Entity.isEntityDefined(project, "Project is defined");
        // Gets the properties for this project
        Resources<Resource<Property<?>>> properties = asUser().with(project.id(), ProjectView.class).call(() ->
                        controller.getProperties(ProjectEntityType.PROJECT, project.getId())
        );
        // Checks there is at least the Jenkins Job property
        assertNotNull("Properties should not be null", properties);
        Optional<Property<?>> property = properties.getResources().stream()
                .map(Resource::getData)
                .filter(p -> "Jenkins Job".equals(p.getType().getName()))
                .findFirst();
        assertTrue("At least the Jenkins Job property should have been found", property.isPresent());
        assertFalse("The Jenkins Job property should not be editable", property.get().isEditable());
    }

    /**
     * Edition form for an existing property.
     */
    @Test
    public void property_edition_form_for_an_existing_property() throws Exception {
        // Creates a project
        Project project = asUser().with(ProjectCreation.class).call(() -> structureService.newProject(
                Project.of(nameDescription())
        ));
        // Creates a Jenkins configuration
        String configurationName = uid("C");
        asUser().with(GlobalSettings.class).call(() -> jenkinsController.newConfiguration(
                new JenkinsConfiguration(
                        configurationName,
                        "http://jenkins",
                        "",
                        ""
                )
        ));
        asUser()
                .with(project.id(), ProjectConfig.class)
                .call(
                        () -> {
                            controller.editProperty(ProjectEntityType.PROJECT, project.getId(), JenkinsJobPropertyType.class.getName(),
                                    object()
                                            .with("configuration", configurationName)
                                            .with("job", "MyJob")
                                            .end()
                            );
                            // Gets the property
                            Resource<Property<?>> propertyResource = controller.getPropertyValue(ProjectEntityType.PROJECT, project.getId(), JenkinsJobPropertyType.class.getName());
                            assertNotNull(propertyResource);
                            @SuppressWarnings("unchecked")
                            Property<JenkinsJobProperty> property = (Property<JenkinsJobProperty>) propertyResource.getData();
                            // Checks the property
                            assertFalse(property.isEmpty());
                            JenkinsJobProperty value = property.getValue();
                            assertNotNull(value);
                            // Checks the property content
                            assertEquals("MyJob", value.getJob());
                            assertEquals(configurationName, value.getConfiguration().getName());
                            assertEquals("http://jenkins/jobs/MyJob", value.getUrl());
                            // Gets the edition form
                            Form form = controller.getPropertyEditionForm(ProjectEntityType.PROJECT, project.getId(), JenkinsJobPropertyType.class.getName());
                            assertEquals(2, form.getFields().size());
                            {
                                Field f = form.getField("configuration");
                                assertNotNull(f);
                                assertEquals(configurationName, f.getValue());
                            }
                            {
                                Field f = form.getField("job");
                                assertNotNull(f);
                                assertEquals("MyJob", f.getValue());
                            }
                            // End
                            return null;
                        }
                );
    }

}
