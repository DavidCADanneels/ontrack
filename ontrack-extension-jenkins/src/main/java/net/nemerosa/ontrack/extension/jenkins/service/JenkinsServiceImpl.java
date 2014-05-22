package net.nemerosa.ontrack.extension.jenkins.service;

import net.nemerosa.ontrack.extension.jenkins.model.JenkinsConfiguration;
import net.nemerosa.ontrack.extension.jenkins.model.JenkinsConfigurationNotFoundException;
import net.nemerosa.ontrack.extension.jenkins.model.JenkinsService;
import net.nemerosa.ontrack.model.security.GlobalSettings;
import net.nemerosa.ontrack.model.security.SecurityService;
import net.nemerosa.ontrack.model.support.ConfigurationRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

// TODO This can be a generic service, parameterized only by the configuration class
@Service
@Transactional
public class JenkinsServiceImpl implements JenkinsService {

    private final ConfigurationRepository configurationRepository;
    private final SecurityService securityService;

    @Autowired
    public JenkinsServiceImpl(ConfigurationRepository configurationRepository, SecurityService securityService) {
        this.configurationRepository = configurationRepository;
        this.securityService = securityService;
    }

    @Override
    public Collection<JenkinsConfiguration> getConfigurations() {
        securityService.checkGlobalFunction(GlobalSettings.class);
        return configurationRepository.list(JenkinsConfiguration.class);
    }

    @Override
    public JenkinsConfiguration newConfiguration(JenkinsConfiguration configuration) {
        securityService.checkGlobalFunction(GlobalSettings.class);
        return configurationRepository.save(configuration);
    }

    @Override
    public JenkinsConfiguration getConfiguration(String name) {
        securityService.checkGlobalFunction(GlobalSettings.class);
        return configurationRepository
                .find(JenkinsConfiguration.class, name)
                .orElseThrow(() -> new JenkinsConfigurationNotFoundException(name));
    }

    @Override
    public void deleteConfiguration(String name) {
        securityService.checkGlobalFunction(GlobalSettings.class);
        configurationRepository.delete(JenkinsConfiguration.class, name);
    }

    /**
     * Gets the former password if new password is blank for the same user. For a new user,
     * a blank password can be accepted.
     */
    @Override
    public void updateConfiguration(String name, JenkinsConfiguration configuration) {
        securityService.checkGlobalFunction(GlobalSettings.class);
        Validate.isTrue(StringUtils.equals(name, configuration.getName()), "Configuration name must match");
        JenkinsConfiguration configToSave;
        if (StringUtils.isBlank(configuration.getPassword())) {
            JenkinsConfiguration oldConfig = getConfiguration(name);
            if (StringUtils.equals(oldConfig.getUser(), configuration.getUser())) {
                configToSave = configuration.withPassword(oldConfig.getPassword());
            } else {
                configToSave = configuration;
            }
        } else {
            configToSave = configuration;
        }
        configurationRepository.save(configToSave);
    }

}
