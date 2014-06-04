package net.nemerosa.ontrack.boot.ui;

import net.nemerosa.ontrack.model.form.DateTime;
import net.nemerosa.ontrack.model.form.Form;
import net.nemerosa.ontrack.model.form.Selection;
import net.nemerosa.ontrack.model.security.SecurityService;
import net.nemerosa.ontrack.model.structure.*;
import net.nemerosa.ontrack.model.support.Time;
import net.nemerosa.ontrack.ui.controller.AbstractResourceController;
import net.nemerosa.ontrack.ui.resource.Link;
import net.nemerosa.ontrack.ui.resource.Resource;
import net.nemerosa.ontrack.ui.resource.ResourceCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

@RestController
@RequestMapping("/structure")
public class BuildController extends AbstractResourceController {

    private final StructureService structureService;
    private final SecurityService securityService;

    @Autowired
    public BuildController(StructureService structureService, SecurityService securityService) {
        this.structureService = structureService;
        this.securityService = securityService;
    }

    // Builds

    @RequestMapping(value = "branches/{branchId}/builds/create", method = RequestMethod.GET)
    public Form newBuildForm(@PathVariable ID branchId) {
        // Checks the branch does exist
        structureService.getBranch(branchId);
        // Returns the form
        return Build.form();
    }

    @RequestMapping(value = "branches/{branchId}/builds/create", method = RequestMethod.POST)
    public Build newBuild(@PathVariable ID branchId, @RequestBody NameDescription nameDescription) {
        // Gets the holding branch
        Branch branch = structureService.getBranch(branchId);
        // Build signature
        Signature signature = securityService.getCurrentSignature();
        // Creates a new build
        Build build = Build.of(branch, nameDescription, signature);
        // Saves it into the repository
        return structureService.newBuild(build);
    }

    @RequestMapping(value = "builds/{buildId}", method = RequestMethod.GET)
    public Build getBuild(@PathVariable ID buildId) {
        return structureService.getBuild(buildId);
    }

    // Promoted runs

    @RequestMapping(value = "builds/{buildId}/promotionRun/last", method = RequestMethod.GET)
    public ResourceCollection<PromotionRun> getLastPromotionRuns(@PathVariable ID buildId) {
        return ResourceCollection.of(
                structureService.getLastPromotionRunsForBuild(buildId)
                        .stream()
                        .map(this::toPromotionRunResource)
                        .collect(Collectors.toList()),
                uri(on(getClass()).getLastPromotionRuns(buildId))
        ).forView(Build.class);
    }

    @RequestMapping(value = "builds/{buildId}/promotionRun/create", method = RequestMethod.GET)
    public Form newPromotionRunForm(@PathVariable ID buildId) {
        Build build = structureService.getBuild(buildId);
        return Form.create()
                .with(
                        Selection.of("promotionLevel")
                                .label("Promotion level")
                                .items(structureService.getPromotionLevelListForBranch(build.getBranch().getId()))
                )
                .with(
                        DateTime.of("dateTime")
                                .label("Date/time")
                                .value(Time.now())
                                .minuteStep(15)
                )
                .description();
    }

    @RequestMapping(value = "builds/{buildId}/promotionRun/create", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public Resource<PromotionRun> newPromotionRun(@PathVariable ID buildId, @RequestBody PromotionRunRequest promotionRunRequest) {
        // Gets the build
        Build build = structureService.getBuild(buildId);
        // Gets the promotion level
        PromotionLevel promotionLevel = structureService.getPromotionLevel(ID.of(promotionRunRequest.getPromotionLevel()));
        // Promotion run to create
        PromotionRun promotionRun = PromotionRun.of(
                build,
                promotionLevel,
                securityService.getCurrentSignature().withTime(promotionRunRequest.getDateTime()),
                promotionRunRequest.getDescription()
        );
        // Creation
        promotionRun = structureService.newPromotionRun(promotionRun);
        // OK
        return toPromotionRunResource(promotionRun);
    }

    @RequestMapping(value = "promotionRuns/{promotionRunId}", method = RequestMethod.GET)
    public Resource<PromotionRun> getPromotionRun(@PathVariable ID promotionRunId) {
        return toPromotionRunResource(structureService.getPromotionRun(promotionRunId));
    }

    // Validation runs

    private Resource<PromotionRun> toPromotionRunResource(PromotionRun promotionRun) {
        return Resource.of(
                promotionRun,
                uri(on(getClass()).getPromotionRun(promotionRun.getId()))
        )
                .with(Link.IMAGE_LINK, uri(on(PromotionLevelController.class).getPromotionLevelImage_(promotionRun.getPromotionLevel().getId())))
                ;
    }
}
