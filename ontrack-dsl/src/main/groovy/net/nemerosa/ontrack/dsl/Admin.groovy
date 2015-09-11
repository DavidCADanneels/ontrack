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
     * Gets the list of accounts
     */

    List<Account> getAccounts() {
        ontrack.get('accounts').resources.collect {
            new Account(ontrack, it)
        }
    }

    /**
     * Creating or updating an account
     */
    Account account(String name, String fullName, String email, String password = '') {
        // Gets the groups
        def accounts = ontrack.get('accounts')
        // Looks for an existing account
        def account = accounts.resources.find { it.name == name }
        if (account != null) {
            // Update
            new Account(
                    ontrack,
                    ontrack.put(
                            account._update as String,
                            [
                                    name    : name,
                                    fullName: fullName,
                                    email   : email,
                                    password: password
                            ]
                    )
            )
        } else {
            // Creation
            new Account(
                    ontrack,
                    ontrack.post(
                            accounts._create as String,
                            [
                                    name    : name,
                                    fullName: fullName,
                                    email   : email,
                                    password: password
                            ]
                    )
            )
        }
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
     * Creating or updating a group
     */
    AccountGroup accountGroup(String name, String description) {
        // Gets the groups
        def groups = ontrack.get('accounts/groups')
        // Looks for an existing group
        def group = groups.resources.find { it.name == name }
        if (group != null) {
            // Update
            new AccountGroup(
                    ontrack,
                    ontrack.put(
                            group._update as String,
                            [
                                    name       : name,
                                    description: description
                            ]
                    )
            )
        } else {
            // Creation
            new AccountGroup(
                    ontrack,
                    ontrack.post(
                            groups._create as String,
                            [
                                    name       : name,
                                    description: description
                            ]
                    )
            )
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
        if (mapping != null) {
            // Update
            new GroupMapping(
                    ontrack,
                    ontrack.put(
                            mapping._update as String,
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
                            mappings._create as String,
                            [
                                    name : name,
                                    group: group.id
                            ]
                    )
            )
        }
    }

}
