package net.nemerosa.ontrack.extension.jenkins;

import net.nemerosa.ontrack.extension.jenkins.client.JenkinsClient;
import net.nemerosa.ontrack.extension.jenkins.client.JenkinsClientFactory;
import net.nemerosa.ontrack.extension.support.AbstractConfigurationService;
import net.nemerosa.ontrack.model.events.EventFactory;
import net.nemerosa.ontrack.model.events.EventPostService;
import net.nemerosa.ontrack.model.security.EncryptionService;
import net.nemerosa.ontrack.model.security.SecurityService;
import net.nemerosa.ontrack.model.support.ConfigurationRepository;
import net.nemerosa.ontrack.model.support.ConnectionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class JenkinsConfigurationServiceImpl extends AbstractConfigurationService<JenkinsConfiguration> implements JenkinsConfigurationService {

    private final JenkinsClientFactory jenkinsClientFactory;

    @Autowired
    public JenkinsConfigurationServiceImpl(ConfigurationRepository configurationRepository, SecurityService securityService, EncryptionService encryptionService, EventPostService eventPostService, EventFactory eventFactory, JenkinsClientFactory jenkinsClientFactory) {
        super(JenkinsConfiguration.class, configurationRepository, securityService, encryptionService, eventPostService, eventFactory);
        this.jenkinsClientFactory = jenkinsClientFactory;
    }

    @Override
    protected ConnectionResult validate(JenkinsConfiguration configuration) {
        JenkinsClient client = jenkinsClientFactory.getClient(configuration);
        try {
            // Gets the basic info
            client.getInfo();
            // OK
            return ConnectionResult.ok();
        } catch (Exception ex) {
            return ConnectionResult.error(ex.getMessage());
        }
    }
}
