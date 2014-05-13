package net.nemerosa.ontrack.boot.ui;

import net.nemerosa.ontrack.model.structure.ID;
import net.nemerosa.ontrack.model.structure.NameDescription;
import net.nemerosa.ontrack.model.structure.Project;
import net.nemerosa.ontrack.model.structure.StructureRepository;
import net.nemerosa.ontrack.ui.controller.AbstractResourceController;
import net.nemerosa.ontrack.ui.resource.Link;
import net.nemerosa.ontrack.ui.resource.Resource;
import net.nemerosa.ontrack.ui.resource.ResourceCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

@RestController
@RequestMapping("/structure")
public class StructureAPIController extends AbstractResourceController implements StructureAPI {

    private final StructureRepository structureRepository;

    @Autowired
    public StructureAPIController(StructureRepository structureRepository) {
        this.structureRepository = structureRepository;
    }

    @Override
    @RequestMapping(value = "projects", method = RequestMethod.GET)
    public ResourceCollection<Project> getProjectList() {
        return ResourceCollection.of(
                structureRepository.getProjectList().stream().map(this::toProjectResource),
                uri(on(StructureAPIController.class).getProjectList())
        )
                // TODO Authorization
                .with(Link.CREATE, uri(on(StructureAPIController.class).newProject(null)));
    }

    @Override
    @RequestMapping(value = "projects/create", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public Resource<Project> newProject(@RequestBody NameDescription nameDescription) {
        // Creates a new project instance
        Project project = Project.of(nameDescription);
        // Saves it into the repository
        project = structureRepository.newProject(project);
        // OK
        return toProjectResource(project);
    }

    @Override
    @RequestMapping(value = "projects/{projectId}", method = RequestMethod.GET)
    public Resource<Project> getProject(@PathVariable ID projectId) {
        // Gets from the repository
        Project project = structureRepository.getProject(projectId);
        // As resource
        return toProjectResource(project);
    }

    private Resource<Project> toProjectResource(Project project) {
        return Resource.of(
                project,
                uri(on(StructureAPIController.class).getProject(project.getId()))
        );
        // TODO Update link
        // TODO Delete link
        // TODO View link
    }
}
