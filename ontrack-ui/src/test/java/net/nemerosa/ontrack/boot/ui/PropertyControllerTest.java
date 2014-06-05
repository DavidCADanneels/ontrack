package net.nemerosa.ontrack.boot.ui;

import net.nemerosa.ontrack.model.security.ProjectConfig;
import net.nemerosa.ontrack.model.security.ProjectCreation;
import net.nemerosa.ontrack.model.structure.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for the property controller.
 */
public class PropertyControllerTest extends AbstractWebTestSupport {

    @Autowired
    private PropertyController controller;

    @Autowired
    private StructureService structureService;

    /**
     * List of editable properties for a project.
     */
    @Test
    public void project_properties() throws Exception {
        Project project = asUser().with(ProjectCreation.class).call(() -> structureService.newProject(
                Project.of(nameDescription())
        ));
        Entity.isEntityDefined(project, "Project is defined");
        // Gets the editable properties for this project
        List<PropertyTypeDescriptor> properties = asUser().with(project.id(), ProjectConfig.class).call(() ->
                        controller.getEditableProperties(ProjectEntityType.PROJECT, project.getId())
        );
        // Checks there is at least the Jenkins Job property
        assertNotNull("Editable properties should not be null", properties);
        assertTrue(
                "At least the Jenkins Job property should have been found",
                properties.stream()
                        .filter(p -> "Jenkins Job".equals(p.getName()))
                        .findFirst().isPresent()
        );
    }

}
