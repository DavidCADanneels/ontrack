package net.nemerosa.ontrack.extension.jenkins;

import net.nemerosa.ontrack.extension.api.UserMenuExtension;
import net.nemerosa.ontrack.extension.support.AbstractExtension;
import net.nemerosa.ontrack.model.security.Action;
import net.nemerosa.ontrack.model.security.GlobalFunction;
import net.nemerosa.ontrack.model.security.GlobalSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JenkinsUserMenuExtension extends AbstractExtension implements UserMenuExtension {

    @Autowired
    public JenkinsUserMenuExtension(JenkinsExtensionFeature feature) {
        super(feature);
    }

    @Override
    public Class<? extends GlobalFunction> getGlobalFunction() {
        return GlobalSettings.class;
    }

    @Override
    public Action getAction() {
        return Action.of("jenkins-management", "Jenkins settings", "management");
    }
}
