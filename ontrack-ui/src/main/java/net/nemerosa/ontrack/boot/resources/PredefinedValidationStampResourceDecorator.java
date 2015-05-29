package net.nemerosa.ontrack.boot.resources;

import net.nemerosa.ontrack.boot.ui.PredefinedValidationStampController;
import net.nemerosa.ontrack.model.security.GlobalSettings;
import net.nemerosa.ontrack.model.structure.PredefinedValidationStamp;
import net.nemerosa.ontrack.ui.resource.AbstractResourceDecorator;
import net.nemerosa.ontrack.ui.resource.Link;
import net.nemerosa.ontrack.ui.resource.ResourceContext;

import java.util.List;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

public class PredefinedValidationStampResourceDecorator extends AbstractResourceDecorator<PredefinedValidationStamp> {

    protected PredefinedValidationStampResourceDecorator() {
        super(PredefinedValidationStamp.class);
    }

    @Override
    public List<Link> links(PredefinedValidationStamp validationStamp, ResourceContext resourceContext) {
        return resourceContext.links()
                .self(on(PredefinedValidationStampController.class).getValidationStamp(validationStamp.getId()))
                        // TODO Image link
                        // .link(Link.IMAGE_LINK, on(PredefinedValidationStampController.class).getValidationStampImage_(validationStamp.getId()))
                        // Update link
                .link(Link.UPDATE, on(PredefinedValidationStampController.class).updateValidationStampForm(validationStamp.getId()), GlobalSettings.class)
                        // TODO Delete link
                        // .delete(on(PredefinedValidationStampController.class).deleteValidationStamp(validationStamp.getId()), ValidationStampDelete.class, validationStamp.projectId())
                        // OK
                .build();
    }

}
