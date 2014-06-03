package net.nemerosa.ontrack.boot.ui;

import net.nemerosa.ontrack.model.security.BranchCreate;
import net.nemerosa.ontrack.model.security.ProjectCreation;
import net.nemerosa.ontrack.model.structure.Branch;
import net.nemerosa.ontrack.model.structure.NameDescription;
import net.nemerosa.ontrack.model.structure.Project;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.Assert.*;

public class BranchControllerTest extends AbstractWebTestSupport {

    @Autowired
    private ProjectController projectController;
    @Autowired
    private BranchController controller;

    @Test
    public void createBranch() throws Exception {
        // Project
        Project project = asUser().with(ProjectCreation.class).call(() -> projectController.newProject(nameDescription()));
        // Branch
        NameDescription nameDescription = nameDescription();
        Branch branch = asUser()
                .with(project.id(), BranchCreate.class)
                .call(() -> controller.newBranch(project.getId(), nameDescription));
        // Checks the branch
        checkBranchResource(branch, nameDescription);
    }

    @Test(expected = AccessDeniedException.class)
    public void createBranch_denied() throws Exception {
        // Project
        Project project = asUser().with(ProjectCreation.class).call(() -> projectController.newProject(nameDescription()));
        // Branch
        asUser().call(() -> controller.newBranch(project.getId(), nameDescription()));
    }

    private void checkBranchResource(Branch branch, NameDescription nameDescription) {
        assertNotNull("Branch not null", branch);
        assertNotNull("Branch ID not null", branch.getId());
        assertTrue("Branch ID set", branch.getId().isSet());
        assertEquals("Branch name", nameDescription.getName(), branch.getName());
        assertEquals("Branch description", nameDescription.getDescription(), branch.getDescription());
    }

}
