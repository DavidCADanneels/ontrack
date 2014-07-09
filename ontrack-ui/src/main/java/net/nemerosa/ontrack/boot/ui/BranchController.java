package net.nemerosa.ontrack.boot.ui;

import net.nemerosa.ontrack.extension.api.BuildDiffExtension;
import net.nemerosa.ontrack.extension.api.ExtensionManager;
import net.nemerosa.ontrack.model.buildfilter.BuildFilter;
import net.nemerosa.ontrack.model.buildfilter.BuildFilterService;
import net.nemerosa.ontrack.model.form.Form;
import net.nemerosa.ontrack.model.security.Action;
import net.nemerosa.ontrack.model.security.BranchCreate;
import net.nemerosa.ontrack.model.security.SecurityService;
import net.nemerosa.ontrack.model.structure.*;
import net.nemerosa.ontrack.ui.controller.AbstractResourceController;
import net.nemerosa.ontrack.ui.resource.Link;
import net.nemerosa.ontrack.ui.resource.Resources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

@RestController
@RequestMapping("/structure")
public class BranchController extends AbstractResourceController {

    private final StructureService structureService;
    private final BuildFilterService buildFilterService;
    private final ExtensionManager extensionManager;
    private final SecurityService securityService;

    @Autowired
    public BranchController(StructureService structureService, BuildFilterService buildFilterService, ExtensionManager extensionManager, SecurityService securityService) {
        this.structureService = structureService;
        this.buildFilterService = buildFilterService;
        this.extensionManager = extensionManager;
        this.securityService = securityService;
    }

    @RequestMapping(value = "projects/{projectId}/branches", method = RequestMethod.GET)
    public Resources<Branch> getBranchListForProject(@PathVariable ID projectId) {
        return Resources.of(
                structureService.getBranchesForProject(projectId),
                uri(on(BranchController.class).getBranchListForProject(projectId))
        )
                // Create
                .with(
                        Link.CREATE,
                        uri(on(BranchController.class).newBranch(projectId, null)),
                        securityService.isProjectFunctionGranted(projectId.getValue(), BranchCreate.class)
                )
                ;
    }

    @RequestMapping(value = "projects/{projectId}/branches/create", method = RequestMethod.GET)
    public Form newBranchForm(@PathVariable ID projectId) {
        // Checks the project exists
        structureService.getProject(projectId);
        // Returns the form
        return Branch.form();
    }

    @RequestMapping(value = "projects/{projectId}/branches/create", method = RequestMethod.POST)
    public Branch newBranch(@PathVariable ID projectId, @RequestBody NameDescription nameDescription) {
        // Gets the project
        Project project = structureService.getProject(projectId);
        // Creates a new branch instance
        Branch branch = Branch.of(project, nameDescription);
        // Saves it into the repository
        branch = structureService.newBranch(branch);
        // OK
        return branch;
    }

    @RequestMapping(value = "branches/{branchId}", method = RequestMethod.GET)
    public Branch getBranch(@PathVariable ID branchId) {
        return structureService.getBranch(branchId);
    }

    @RequestMapping(value = "branches/{branchId}/update", method = RequestMethod.GET)
    public Form getUpdateForm(@PathVariable ID branchId) {
        // Loads the branch and gets a form
        return structureService.getBranch(branchId).toForm();
    }

    @RequestMapping(value = "branches/{branchId}/update", method = RequestMethod.PUT)
    public Branch updateBranch(@PathVariable ID branchId, @RequestBody NameDescription form) {
        // Loads and updates branch
        Branch branch = structureService.getBranch(branchId).update(form);
        // Saves the branch
        structureService.saveBranch(branch);
        // OK
        return branch;
    }

    @RequestMapping(value = "branches/{branchId}/status", method = RequestMethod.GET)
    public BranchStatusView getBranchStatusView(@PathVariable ID branchId) {
        return structureService.getBranchStatusView(structureService.getBranch(branchId));
    }


    /**
     * FIXME Returns the list of existing filters for this branch and the current user, and the list
     * of forms that can be used to create new ones.
     * @param branchId ID of the branch to get the filter for.
     */
    @RequestMapping(value = "branches/{branchId}/filter", method = RequestMethod.GET)
    public void buildFilters(@PathVariable ID branchId) {

    }

    @RequestMapping(value = "branches/{branchId}/view", method = RequestMethod.GET)
    // TODO Filter
    public BranchBuildView buildView(@PathVariable ID branchId) {
        // Gets the branch
        Branch branch = getBranch(branchId);
        // TODO Defines the filter for the service
        BuildFilter buildFilter = buildFilterService.defaultFilter();
        // Gets the list of builds
        List<Build> builds = structureService.getFilteredBuilds(branchId, buildFilter);
        // Gets the list of build diff actions
        List<Action> buildDiffActions = extensionManager.getExtensions(BuildDiffExtension.class)
                .stream()
                .filter(extension -> extension.apply(branch))
                .map(this::resolveExtensionAction)
                .collect(Collectors.toList());
        // Gets the views for each build
        return new BranchBuildView(
                builds.stream()
                        .map(structureService::getBuildView)
                        .collect(Collectors.toList()),
                buildDiffActions
        );
    }

}
