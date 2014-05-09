package net.nemerosa.ontrack.boot.ui;

import net.nemerosa.ontrack.boot.resource.Resource;
import net.nemerosa.ontrack.model.structure.Branch;
import net.nemerosa.ontrack.model.structure.NameDescription;
import net.nemerosa.ontrack.model.structure.Project;
import net.nemerosa.ontrack.model.structure.StructureFactory;
import net.nemerosa.ontrack.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/ui/structure")
public class UIStructureController implements UIStructure {

    private final ResourceAssembler resourceAssembler;
    private final StructureFactory structureFactory;

    public UIStructureController(ResourceAssembler resourceAssembler, StructureFactory structureFactory) {
        this.resourceAssembler = resourceAssembler;
        this.structureFactory = structureFactory;
    }

    @Override
    public Resource<Project> newProject(NameDescription nameDescription) {
        // Creates a new project instance
        Project project = structureFactory.newProject(nameDescription);
        // TODO Saves it into the repository
        // Gets the resource
        return resourceAssembler.toProjectResource(project);
    }
}
