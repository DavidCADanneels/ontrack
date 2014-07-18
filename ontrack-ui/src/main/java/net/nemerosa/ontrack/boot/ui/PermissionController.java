package net.nemerosa.ontrack.boot.ui;

import net.nemerosa.ontrack.model.Ack;
import net.nemerosa.ontrack.model.security.*;
import net.nemerosa.ontrack.ui.controller.AbstractResourceController;
import net.nemerosa.ontrack.ui.resource.Resources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

@RestController
@RequestMapping("/accounts/permissions")
public class PermissionController extends AbstractResourceController {

    private final AccountService accountService;
    private final RolesService rolesService;

    @Autowired
    public PermissionController(AccountService accountService, RolesService rolesService) {
        this.accountService = accountService;
        this.rolesService = rolesService;
    }

    /**
     * List of global permissions
     */
    @RequestMapping(value = "globals", method = RequestMethod.GET)
    public Resources<GlobalPermission> getGlobalPermissions() {
        return Resources.of(
                accountService.getGlobalPermissions(),
                uri(on(PermissionController.class).getGlobalPermissions())
        ).with("_globalRoles", uri(on(PermissionController.class).getGlobalRoles()));
    }

    /**
     * List of global roles
     */
    @RequestMapping(value = "globals/roles", method = RequestMethod.GET)
    public Resources<GlobalRole> getGlobalRoles() {
        return Resources.of(
                rolesService.getGlobalRoles(),
                uri(on(PermissionController.class).getGlobalRoles())
        );
    }

    /**
     * Looking for a permission target
     */
    @RequestMapping(value = "search/{token:.*}", method = RequestMethod.GET)
    public Resources<PermissionTarget> searchPermissionTargets(@PathVariable String token) {
        return Resources.of(
                accountService.searchPermissionTargets(token),
                uri(on(PermissionController.class).searchPermissionTargets(token))
        );
    }

    /**
     * Saving a global permission
     */
    @RequestMapping(value = "globals/{type}/{id}", method = RequestMethod.PUT)
    public Ack saveGlobalPermission(@PathVariable PermissionTargetType type, @PathVariable int id, @RequestBody PermissionInput input) {
        return accountService.saveGlobalPermission(type, id, input);
    }

    /**
     * TODO Deleting a global permission
     */
    @RequestMapping(value = "globals/{type}/{id}", method = RequestMethod.DELETE)
    public Ack deleteGlobalPermission(@PathVariable PermissionTargetType type, @PathVariable int id) {
        return Ack.NOK;
    }

}
