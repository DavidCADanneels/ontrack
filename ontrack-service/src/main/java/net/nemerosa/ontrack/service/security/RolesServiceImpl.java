package net.nemerosa.ontrack.service.security;

import net.nemerosa.ontrack.model.security.*;
import net.nemerosa.ontrack.model.support.StartupService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Management of the roles and functions.
 */
@Service
public class RolesServiceImpl implements RolesService, StartupService {

    /**
     * Index of global roles
     */
    private final Map<String, GlobalRole> globalRoles = new LinkedHashMap<>();

    @Override
    public List<GlobalRole> getGlobalRoles() {
        // FIXME Method net.nemerosa.ontrack.service.security.RolesServiceImpl.getGlobalRoles
        return null;
    }

    @Override
    public Optional<GlobalRole> getGlobalRole(String id) {
        // FIXME Method net.nemerosa.ontrack.service.security.RolesServiceImpl.getGlobalRole
        return null;
    }

    @Override
    public List<Class<? extends GlobalFunction>> getGlobalFunctions() {
        return defaultGlobalFunctions;
    }

    @Override
    public List<Class<? extends ProjectFunction>> getProjectFunctions() {
        return defaultProjectFunctions;
    }

    @Override
    public String getName() {
        return "Roles";
    }

    @Override
    public int startupOrder() {
        return 50;
    }

    @Override
    public void start() {
        // Global roles
        initGlobalRoles();
        // TODO Project roles
    }

    private void initGlobalRoles() {

        // Administrator
        register("ADMINISTRATOR", "Administrator",
                "An administrator is allowed to do everything in the application.",
                getGlobalFunctions(),
                getProjectFunctions());

        // Controller
        register("CONTROLLER", "Controller",
                "A controller, is allowed to create builds, promotion runs and validation runs. This role is " +
                        "typically granted to continuous integration tools.",
                Arrays.asList(
                        // No global function
                ),
                Arrays.asList(
                        BuildCreate.class,
                        PromotionRunCreate.class,
                        ValidationRunCreate.class
                )
        );

    }

    private void register(String id, String name, String description, List<Class<? extends GlobalFunction>> globalFunctions, List<Class<? extends ProjectFunction>> projectFunctions) {
        register(new GlobalRole(
                id,
                name,
                description,
                new LinkedHashSet<>(globalFunctions),
                new LinkedHashSet<>(projectFunctions)
        ));
    }

    private void register(GlobalRole role) {
        globalRoles.put(role.getId(), role);
    }
}
