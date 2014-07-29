package net.nemerosa.ontrack.extension.svn.property;

import net.nemerosa.ontrack.extension.support.AbstractPropertyTypeExtension;
import net.nemerosa.ontrack.extension.svn.SVNExtensionFeature;
import net.nemerosa.ontrack.model.structure.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SVNRevisionChangeLogIssueValidatorExtension extends AbstractPropertyTypeExtension<SVNRevisionChangeLogIssueValidatorConfig> {

    @Autowired
    public SVNRevisionChangeLogIssueValidatorExtension(SVNExtensionFeature extensionFeature, PropertyService propertyService) {
        super(extensionFeature, new SVNRevisionChangeLogIssueValidator(propertyService));
    }

}
