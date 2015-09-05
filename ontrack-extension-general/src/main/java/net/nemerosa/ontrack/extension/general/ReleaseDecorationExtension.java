package net.nemerosa.ontrack.extension.general;

import net.nemerosa.ontrack.extension.api.DecorationExtension;
import net.nemerosa.ontrack.extension.support.AbstractExtension;
import net.nemerosa.ontrack.model.structure.*;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

@Component
public class ReleaseDecorationExtension extends AbstractExtension implements DecorationExtension<String> {

    private final PropertyService propertyService;

    @Autowired
    public ReleaseDecorationExtension(GeneralExtensionFeature extensionFeature, PropertyService propertyService) {
        super(extensionFeature);
        this.propertyService = propertyService;
    }

    @Override
    public EnumSet<ProjectEntityType> getScope() {
        return EnumSet.of(ProjectEntityType.BUILD);
    }

    @Override
    public List<Decoration<String>> getDecorations(ProjectEntity entity) {
        // Argument check
        Validate.isTrue(entity instanceof Build, "Expecting build");
        // Gets the `release` property
        Property<ReleaseProperty> property = propertyService.getProperty(entity, ReleasePropertyType.class);
        if (property.isEmpty()) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(
                    Decoration.of(
                            this,
                            property.getValue().getName()
                    )
            );
        }
    }

}
