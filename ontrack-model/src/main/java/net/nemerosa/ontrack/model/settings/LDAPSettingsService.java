package net.nemerosa.ontrack.model.settings;

import net.nemerosa.ontrack.model.form.Form;

public interface LDAPSettingsService {

    LDAPSettings getSettings();

    void saveSettings(LDAPSettings ldapSettings);

    Form getSettingsForm();

}
