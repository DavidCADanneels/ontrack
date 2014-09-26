package net.nemerosa.ontrack.service.security.ldap;

import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;

public interface LDAPProviderFactory {

    LdapAuthenticationProvider getProvider();

}
