package net.nemerosa.ontrack.service.security

import net.nemerosa.ontrack.it.AbstractServiceTestSupport
import net.nemerosa.ontrack.model.security.*
import org.junit.Assert.*
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.lang.IllegalStateException

open class RolesServiceIT : AbstractServiceTestSupport() {

    companion object {
        val newGlobalRole = "NEW_GLOBAL"
        val newProjectRole = "NEW_PROJECT"
    }

    @Autowired
    private lateinit var rolesService: RolesService

    @Autowired
    private lateinit var securityService: SecurityService

    interface TestGlobalFunction : GlobalFunction
    interface TestProject1Function : ProjectFunction
    interface TestProject2Function : ProjectFunction
    @CoreFunction
    interface TestProjectCoreFunction : ProjectFunction

    @Configuration
    open class RoleTestContributors {
        @Bean
        open fun roleContributor(): RoleContributor {
            return object : RoleContributor {
                override fun getGlobalRoles(): List<RoleDefinition> = listOf(
                        RoleDefinition(newGlobalRole, "New global role", "Test for a new global role")
                )

                override fun getProjectRoles(): List<RoleDefinition> = listOf(
                        RoleDefinition(newProjectRole, "New project", "Test for a new project role")
                )

                override fun getGlobalFunctionContributionsForGlobalRole(role: String): List<Class<out GlobalFunction>> =
                        when (role) {
                            Roles.GLOBAL_CONTROLLER -> listOf(TestGlobalFunction::class.java)
                            newGlobalRole -> listOf(ProjectCreation::class.java, TestGlobalFunction::class.java)
                            else -> listOf()
                        }

                override fun getProjectFunctionContributionsForGlobalRole(role: String): List<Class<out ProjectFunction>> =
                        when (role) {
                            Roles.GLOBAL_CREATOR -> listOf(TestProject1Function::class.java)
                            newGlobalRole -> listOf(TestProject2Function::class.java)
                            else -> listOf()
                        }

                override fun getProjectFunctionContributionsForProjectRole(role: String): List<Class<out ProjectFunction>> =
                        when (role) {
                            Roles.PROJECT_OWNER -> listOf(TestProject2Function::class.java)
                            newProjectRole -> listOf(TestProject2Function::class.java)
                            else -> listOf()
                        }
            }
        }
    }

    @Test
    fun roles_contributions() {
        val globalController = rolesService.getGlobalRole(Roles.GLOBAL_CONTROLLER).orElse(null)
        assertNotNull(globalController)
        assertTrue(TestGlobalFunction::class.java in globalController.globalFunctions)

        val globalCreator = rolesService.getGlobalRole(Roles.GLOBAL_CREATOR).orElse(null)
        assertNotNull(globalCreator)
        assertTrue(TestProject1Function::class.java in globalCreator.projectFunctions)

        val projectOwner = rolesService.getProjectRole(Roles.PROJECT_OWNER).orElse(null)
        assertNotNull(projectOwner)
        assertTrue(TestProject2Function::class.java in projectOwner.functions)
    }

    @Test
    fun only_non_core_functions_are_allowed() {
        val service = RolesServiceImpl(listOf(object : RoleContributor {
            override fun getProjectFunctionContributionsForProjectRole(role: String): List<Class<out ProjectFunction>> =
                    when (role) {
                        Roles.PROJECT_OWNER -> listOf(TestProjectCoreFunction::class.java)
                        else -> listOf()
                    }
        }))
        try {
            service.start()
            fail("It should not have been possible to add a core function to a role.")
        } catch (e: IllegalStateException) {
            assertEquals("A core function cannot be added to an existing role.", e.message)
        }
    }

    @Test
    fun trying_to_add_an_existing_core_function() {
        val service = RolesServiceImpl(listOf(object : RoleContributor {
            override fun getProjectFunctionContributionsForProjectRole(role: String): List<Class<out ProjectFunction>> =
                    when (role) {
                        Roles.PROJECT_PARTICIPANT -> listOf(ProjectConfig::class.java)
                        else -> listOf()
                    }
        }))
        try {
            service.start()
            fail("It should not have been possible to add an existing core function to a role.")
        } catch (e: IllegalStateException) {
            assertEquals("A core function cannot be added to an existing role.", e.message)
        }
    }

    @Test
    fun trying_to_override_a_global_role() {
        val service = RolesServiceImpl(listOf(object : RoleContributor {
            override fun getGlobalRoles(): List<RoleDefinition> =
                listOf(RoleDefinition(Roles.GLOBAL_CREATOR, "Creator", "Overridden creator role"))
        }))
        try {
            service.start()
            fail("It should not have been possible to override an existing global role.")
        } catch (e: IllegalStateException) {
            assertEquals("An existing global role cannot be overridden: " + Roles.GLOBAL_CREATOR, e.message)
        }
    }

    @Test
    fun trying_to_override_a_project_role() {
        val service = RolesServiceImpl(listOf(object : RoleContributor {
            override fun getProjectRoles(): List<RoleDefinition> =
                listOf(RoleDefinition(Roles.PROJECT_OWNER, "Owner", "Overridden owner role"))
        }))
        try {
            service.start()
            fail("It should not have been possible to override an existing project role.")
        } catch (e: IllegalStateException) {
            assertEquals("An existing project role cannot be overridden: " + Roles.PROJECT_OWNER, e.message)
        }
    }

    @Test
    fun testing_a_global_role() {
        val project = doCreateProject()
        asAccount(doCreateAccountWithGlobalRole(Roles.GLOBAL_CONTROLLER)).call {
            assertTrue(securityService.isGlobalFunctionGranted(TestGlobalFunction::class.java))
            assertFalse(securityService.isProjectFunctionGranted(project, TestProject1Function::class.java))
            assertFalse(securityService.isProjectFunctionGranted(project, TestProject2Function::class.java))
        }
    }

    @Test
    fun testing_a_contributed_global_role() {
        val project = doCreateProject()
        asAccount(doCreateAccountWithGlobalRole(newGlobalRole)).call {
            assertTrue(securityService.isGlobalFunctionGranted(TestGlobalFunction::class.java))
            assertTrue(securityService.isGlobalFunctionGranted(ProjectCreation::class.java))
            assertFalse(securityService.isProjectFunctionGranted(project, TestProject1Function::class.java))
            assertTrue(securityService.isProjectFunctionGranted(project, TestProject2Function::class.java))
        }
    }

    @Test
    fun testing_a_global_role_with_project_function() {
        val project = doCreateProject()
        asAccount(doCreateAccountWithGlobalRole(Roles.GLOBAL_CREATOR)).call {
            assertFalse(securityService.isGlobalFunctionGranted(TestGlobalFunction::class.java))
            assertTrue(securityService.isProjectFunctionGranted(project, TestProject1Function::class.java))
            assertFalse(securityService.isProjectFunctionGranted(project, TestProject2Function::class.java))
        }
    }

    @Test
    fun testing_a_project_role() {
        val project = doCreateProject()
        asAccount(doCreateAccountWithProjectRole(project, Roles.PROJECT_OWNER)).call {
            assertFalse(securityService.isGlobalFunctionGranted(TestGlobalFunction::class.java))
            assertFalse(securityService.isProjectFunctionGranted(project, TestProject1Function::class.java))
            assertTrue(securityService.isProjectFunctionGranted(project, TestProject2Function::class.java))
        }
    }

    @Test
    fun testing_a_contributed_project_role() {
        val project = doCreateProject()
        asAccount(doCreateAccountWithProjectRole(project, newProjectRole)).call {
            assertFalse(securityService.isGlobalFunctionGranted(TestGlobalFunction::class.java))
            assertFalse(securityService.isProjectFunctionGranted(project, TestProject1Function::class.java))
            assertTrue(securityService.isProjectFunctionGranted(project, TestProject2Function::class.java))
        }
    }

    @Test
    fun testing_a_neutral_project_role() {
        val project = doCreateProject()
        asAccount(doCreateAccountWithProjectRole(project, Roles.PROJECT_PARTICIPANT)).call {
            assertFalse(securityService.isGlobalFunctionGranted(TestGlobalFunction::class.java))
            assertFalse(securityService.isProjectFunctionGranted(project, TestProject1Function::class.java))
            assertFalse(securityService.isProjectFunctionGranted(project, TestProject2Function::class.java))
        }
    }

    @Test
    fun `Global role contribution`() {
        assertNotNull(rolesService.globalRoles.find { it.id == newGlobalRole })
    }

    @Test
    fun `Project role contribution`() {
        assertNotNull(rolesService.projectRoles.find { it.id == newProjectRole })
    }

}