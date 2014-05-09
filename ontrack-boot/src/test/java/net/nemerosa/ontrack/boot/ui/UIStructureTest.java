package net.nemerosa.ontrack.boot.ui;

import net.nemerosa.ontrack.boot.resource.Resource;
import net.nemerosa.ontrack.it.AbstractITTestSupport;
import net.nemerosa.ontrack.model.structure.NameDescription;
import net.nemerosa.ontrack.model.structure.Project;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UIStructureTest extends AbstractITTestSupport {

    @Autowired
    private UIStructure structure;

    @Test
    public void createProject() {
        NameDescription nameDescription = nameDescription();
        Resource<Project> resource = structure.newProject(nameDescription);
        assertNotNull("Resource not null", resource);
        Project project = resource.getData();
        assertNotNull("Project not null", project);
        assertNotNull("Project ID defined", project.getId());
        assertEquals("Project name", nameDescription.getName(), project.getName());
        assertEquals("Project description", nameDescription.getDescription(), project.getDescription());
    }

}
