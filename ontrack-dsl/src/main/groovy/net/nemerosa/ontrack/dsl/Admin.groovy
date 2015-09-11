package net.nemerosa.ontrack.dsl

/**
 * Administration management.
 */
class Admin {

    private final Ontrack ontrack

    Admin(Ontrack ontrack) {
        this.ontrack = ontrack
    }

    /**
     * Gets the list of groups
     */

    List<AccountGroup> getGroups() {
        ontrack.get('accounts/groups').resources.collect {
            new AccountGroup(ontrack, it)
        }
    }

    /**
     * Gets the list of LDAP mappings
     */
    List<GroupMapping> getLdapMappings() {
        ontrack.get('extension/ldap/ldap-mapping').resources.collect { node ->
            new GroupMapping(ontrack, node)
        }
    }

    /**
     * Creates or updates a LDAP mapping
     * @param name LDAP group name
     * @param groupName Group to map to
     * @return Mapping
     */
    GroupMapping ldapMapping(String name, String groupName) {
        def mappings = ontrack.get('extension/ldap/ldap-mapping')
        // Group ID from the name
        def group = getGroups().find { it.name == groupName }
        if (group == null) {
            throw new AccountGroupNameNotFoundException(groupName)
        }
        // Looks for an existing mapping
        def mapping = mappings.resources.find { it.name == name }
        if (mapping) {
            // Update
            new GroupMapping(
                    ontrack,
                    ontrack.put(
                            mapping._update,
                            [
                                    name : name,
                                    group: group.id
                            ]
                    )
            )
        } else {
            // Creation
            new GroupMapping(
                    ontrack,
                    ontrack.post(
                            mappings._create,
                            [
                                    name : name,
                                    group: group.id
                            ]
                    )
            )
        }
    }

}
