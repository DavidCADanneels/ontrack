package net.nemerosa.ontrack.service.security;

import net.nemerosa.ontrack.it.AbstractServiceTestSupport;
import net.nemerosa.ontrack.model.security.Account;
import net.nemerosa.ontrack.model.security.SecurityService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SecurityServiceIT extends AbstractServiceTestSupport {

    @Autowired
    private SecurityService securityService;

    @Test
    public void getCurrentAccount() throws Exception {
        Account account = asUser().call(securityService::getCurrentAccount);
        assertNotNull(account);
    }

    @Test
    public void getCurrentAccount_none() throws Exception {
        Account account = securityService.getCurrentAccount();
        assertNull(account);
    }

}
