package net.nemerosa.ontrack.service.settings;

import net.nemerosa.ontrack.model.security.GlobalSettings;
import net.nemerosa.ontrack.model.security.SecurityService;
import net.nemerosa.ontrack.model.settings.LDAPSettings;
import net.nemerosa.ontrack.model.settings.SecuritySettings;
import net.nemerosa.ontrack.model.settings.SettingsService;
import net.nemerosa.ontrack.service.Caches;
import net.nemerosa.ontrack.service.support.SettingsInternalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * This service delegates to an internal cached service in order to avoid the cyclic
 * dependency SecurityService &lt;--&gt; SettingsService. We now have
 * SecurityService --&gt; SettingsInternalService and SettingsService --&gt; SecurityService
 * and SettingsService --&gt; SettingsInternalService
 */
@Service
public class SettingsServiceImpl implements SettingsService {

    private final SecurityService securityService;
    private final SettingsInternalService settingsInternalService;

    @Autowired
    public SettingsServiceImpl(SecurityService securityService, SettingsInternalService settingsInternalService) {
        this.securityService = securityService;
        this.settingsInternalService = settingsInternalService;
    }

    @Override
    public SecuritySettings getSecuritySettings() {
        securityService.checkGlobalFunction(GlobalSettings.class);
        return settingsInternalService.getSecuritySettings();
    }

    @Override
    @CacheEvict(value = Caches.SECURITY_SETTINGS, allEntries = true)
    public void saveSecuritySettings(SecuritySettings securitySettings) {
        securityService.checkGlobalFunction(GlobalSettings.class);
        settingsInternalService.saveSecuritySettings(securitySettings);
    }

    @Override
    public LDAPSettings getLDAPSettings() {
        securityService.checkGlobalFunction(GlobalSettings.class);
        return settingsInternalService.getLDAPSettings();
    }

    @Override
    @CacheEvict(value = Caches.LDAP_SETTINGS, allEntries = true)
    public void saveLDAPSettings(LDAPSettings ldapSettings) {
        securityService.checkGlobalFunction(GlobalSettings.class);
        settingsInternalService.saveLDAPSettings(ldapSettings);
    }
}
