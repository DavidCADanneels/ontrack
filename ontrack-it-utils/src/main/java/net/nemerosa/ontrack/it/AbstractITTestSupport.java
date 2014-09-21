package net.nemerosa.ontrack.it;

import net.nemerosa.ontrack.common.RunProfile;
import net.nemerosa.ontrack.model.security.*;
import net.nemerosa.ontrack.model.structure.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.Callable;

import static net.nemerosa.ontrack.test.TestUtils.uid;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        loader = AnnotationConfigContextLoader.class,
        classes = AbstractITTestSupport.AbstractIntegrationTestConfiguration.class)
@Transactional
@ActiveProfiles(profiles = {RunProfile.UNIT_TEST})
public abstract class AbstractITTestSupport extends AbstractJUnit4SpringContextTests {

    @Autowired
    protected AccountService accountService;

    @Autowired
    protected StructureService structureService;

    @Configuration
    @Profile(RunProfile.UNIT_TEST)
    @ComponentScan("net.nemerosa.ontrack")
    public static class AbstractIntegrationTestConfiguration {
    }

    public static NameDescription nameDescription() {
        String uid = uid("");
        return new NameDescription(
                uid,
                String.format("%s description", uid)
        );
    }

    protected Account doCreateAccount() throws Exception {
        return asUser().with(AccountManagement.class).call(() -> {
            String name = uid("A");
            return accountService.create(
                    new AccountInput(
                            name,
                            "Test " + name,
                            name + "@test.com",
                            "test",
                            Collections.emptyList()
                    )
            );
        });
    }

    protected Project doCreateProject() throws Exception {
        return doCreateProject(nameDescription());
    }

    protected Project doCreateProject(NameDescription nameDescription) throws Exception {
        return asUser().with(ProjectCreation.class).call(() -> structureService.newProject(
                Project.of(nameDescription)
        ));
    }

    protected Branch doCreateBranch() throws Exception {
        return doCreateBranch(doCreateProject(), nameDescription());
    }

    protected Branch doCreateBranch(Project project, NameDescription nameDescription) throws Exception {
        return asUser().with(project.id(), BranchCreate.class).call(() -> structureService.newBranch(
                Branch.of(project, nameDescription)
        ));
    }

    protected UserCall asUser() {
        return new UserCall();
    }

    protected <T> T view(ProjectEntity projectEntity, Callable<T> callable) throws Exception {
        return asUser().with(projectEntity.projectId(), ProjectView.class).call(callable);
    }

    protected static interface ContextCall {
        <T> T call(Callable<T> call) throws Exception;
    }

    protected static abstract class AbstractContextCall implements ContextCall {

        @Override
        public <T> T call(Callable<T> call) throws Exception {
            // Gets the current context
            SecurityContext oldContext = SecurityContextHolder.getContext();
            try {
                // Sets the new context
                contextSetup();
                // Call
                return call.call();
            } finally {
                // Restores the context
                SecurityContextHolder.setContext(oldContext);
            }
        }

        protected abstract void contextSetup();
    }

    protected static class AccountCall extends AbstractContextCall {

        protected final Account account;

        public AccountCall(String name, SecurityRole role) {
            account = Account.of(name, name, name + "@test.com", role, AuthenticationSource.none());
        }

        @Override
        protected void contextSetup() {
            SecurityContext context = new SecurityContextImpl();
            TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                    (AccountHolder) () -> account,
                    "",
                    account.getRole().name()
            );
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        }
    }

    protected static class UserCall extends AccountCall {

        public UserCall() {
            super("user", SecurityRole.USER);
        }

        public UserCall with(Class<? extends GlobalFunction> fn) {
            account.withGlobalRole(
                    Optional.of(
                            new GlobalRole(
                                    "test", "Test global role", "",
                                    Collections.singleton(fn),
                                    Collections.emptySet()
                            )
                    )
            );
            return this;
        }

        public UserCall with(int projectId, Class<? extends ProjectFunction> fn) {
            account.withProjectRole(
                    new ProjectRoleAssociation(
                            projectId,
                            new ProjectRole(
                                    "test", "Test", "",
                                    Collections.singleton(fn)
                            )
                    )
            );
            return this;
        }

    }
}
