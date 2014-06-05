package net.nemerosa.ontrack.service.security;

import net.nemerosa.ontrack.model.security.*;
import net.nemerosa.ontrack.model.settings.SecuritySettings;
import net.nemerosa.ontrack.model.structure.Signature;
import net.nemerosa.ontrack.service.support.SettingsInternalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static java.lang.String.format;

@Component
public class SecurityServiceImpl implements SecurityService {

    private final SettingsInternalService settingsInternalService;

    @Autowired
    public SecurityServiceImpl(SettingsInternalService settingsInternalService) {
        this.settingsInternalService = settingsInternalService;
    }

    @Override
    public void checkGlobalFunction(Class<? extends GlobalFunction> fn) {
        if (!isGlobalFunctionGranted(fn)) {
            throw new AccessDeniedException(format("Global function '%s' is not granted.", fn.getSimpleName()));
        }
    }

    @Override
    public boolean isGlobalFunctionGranted(Class<? extends GlobalFunction> fn) {
        // Gets the user
        Account user = getCurrentAccount();
        // Checks
        return user != null && user.isGranted(fn);
    }

    @Override
    public void checkProjectFunction(int projectId, Class<? extends ProjectFunction> fn) {
        if (!isProjectFunctionGranted(projectId, fn)) {
            throw new AccessDeniedException(format("Project function '%s' is not granted", fn.getSimpleName()));
        }
    }

    @Override
    public boolean isProjectFunctionGranted(int projectId, Class<? extends ProjectFunction> fn) {
        // Gets the user
        Account user = getCurrentAccount();
        // Checks
        return (user != null && user.isGranted(projectId, fn)) || isGlobalGrant(fn);
    }

    protected boolean isGlobalGrant(Class<? extends ProjectFunction> fn) {
        SecuritySettings settings = settingsInternalService.getSecuritySettings();
        return settings.isGrantProjectViewToAll() && fn.isAssignableFrom(ProjectView.class);
    }

    @Override
    public Account getCurrentAccount() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && (authentication.getPrincipal() instanceof AccountHolder)) {
            return ((AccountHolder) authentication.getPrincipal()).getAccount();
        } else {
            return null;
        }
    }

    @Override
    public Signature getCurrentSignature() {
        Account account = getCurrentAccount();
        if (account != null) {
            return Signature.of(account.getName());
        } else {
            return Signature.none();
        }
    }

    @Override
    public <T> Supplier<T> runAsAdmin(Supplier<T> supplier) {
        // Gets the current account (if any)
        Account account = getCurrentAccount();
        // Creates a temporary admin context
        SecurityContextImpl adminContext = new SecurityContextImpl();
        adminContext.setAuthentication(new RunAsAdminAuthentication(account));
        // Returns a callable that sets the context before running the target callable
        return withSecurityContext(supplier, adminContext);
    }

    @Override
    public <T> Supplier<T> runner(Supplier<T> supplier) {
        // Current context
        SecurityContext context = SecurityContextHolder.getContext();
        // Uses it
        return withSecurityContext(supplier, context);
    }

    protected <T> Supplier<T> withSecurityContext(final Supplier<T> supplier, final SecurityContext context) {
        // Returns a callable that sets the context before running the target callable
        return () -> {
            SecurityContext oldContext = SecurityContextHolder.getContext();
            try {
                SecurityContextHolder.setContext(context);
                // Result
                return supplier.get();
            } finally {
                SecurityContextHolder.setContext(oldContext);
            }
        };
    }
}
