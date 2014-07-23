package net.nemerosa.ontrack.extension.git.property;

import net.nemerosa.ontrack.extension.git.GitExtensionFeature;
import net.nemerosa.ontrack.extension.git.service.GitConfigurationService;
import net.nemerosa.ontrack.extension.support.AbstractPropertyTypeExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GitProjectConfigurationPropertyTypeExtension extends AbstractPropertyTypeExtension<GitProjectConfigurationProperty> {

    @Autowired
    public GitProjectConfigurationPropertyTypeExtension(GitExtensionFeature extensionFeature, GitConfigurationService configurationService) {
        super(extensionFeature, new GitProjectConfigurationPropertyType(configurationService));
    }
}
