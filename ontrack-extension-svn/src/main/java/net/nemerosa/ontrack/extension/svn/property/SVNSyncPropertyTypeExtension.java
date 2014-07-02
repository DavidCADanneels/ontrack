package net.nemerosa.ontrack.extension.svn.property;

import net.nemerosa.ontrack.extension.support.AbstractPropertyTypeExtension;
import net.nemerosa.ontrack.extension.svn.SVNExtensionFeature;
import net.nemerosa.ontrack.model.structure.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SVNSyncPropertyTypeExtension extends AbstractPropertyTypeExtension<SVNSyncProperty> {

    @Autowired
    public SVNSyncPropertyTypeExtension(SVNExtensionFeature extensionFeature, PropertyService propertyService) {
        super(extensionFeature, new SVNSyncPropertyType(propertyService));
    }

}
