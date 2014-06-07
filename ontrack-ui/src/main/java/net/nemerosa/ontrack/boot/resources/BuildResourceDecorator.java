package net.nemerosa.ontrack.boot.resources;

import net.nemerosa.ontrack.boot.ui.BuildController;
import net.nemerosa.ontrack.boot.ui.PropertyController;
import net.nemerosa.ontrack.boot.ui.ValidationRunController;
import net.nemerosa.ontrack.model.security.PromotionRunCreate;
import net.nemerosa.ontrack.model.security.ValidationRunCreate;
import net.nemerosa.ontrack.model.structure.Build;
import net.nemerosa.ontrack.model.structure.ProjectEntityType;
import net.nemerosa.ontrack.ui.resource.AbstractResourceDecorator;
import net.nemerosa.ontrack.ui.resource.Link;
import net.nemerosa.ontrack.ui.resource.ResourceContext;

import java.util.List;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

public class BuildResourceDecorator extends AbstractResourceDecorator<Build> {

    protected BuildResourceDecorator() {
        super(Build.class);
    }

    @Override
    public List<Link> links(Build build, ResourceContext resourceContext) {
        return resourceContext.links()
                .self(on(BuildController.class).getBuild(build.getId()))
                        // Other linked resources
                .link("_lastPromotionRuns", on(BuildController.class).getLastPromotionRuns(build.getId()))
                .link("_validationRuns", on(ValidationRunController.class).getValidationRuns(build.getId()))
                .link("_validationStampRunViews", on(ValidationRunController.class).getValidationStampRunViews(build.getId()))
                        // Creation of a promoted run
                .link(
                        "_promote",
                        on(BuildController.class).newPromotionRunForm(build.getId()),
                        PromotionRunCreate.class, build.getBranch().getProject().id()
                )
                        // Creation of a validation run
                .link(
                        "_validate",
                        on(ValidationRunController.class).newValidationRunForm(build.getId()),
                        ValidationRunCreate.class, build.getBranch().getProject().id()
                )
                        // Actual properties for this build
                .link("_properties", on(PropertyController.class).getProperties(ProjectEntityType.BUILD, build.getId()))
                        // TODO Update
                        // TODO Delete
                        // OK
                .build();
    }

}
