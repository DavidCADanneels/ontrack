package net.nemerosa.ontrack.acceptance

import com.fasterxml.jackson.databind.JsonNode
import net.nemerosa.ontrack.dsl.Ontrack
import net.nemerosa.ontrack.dsl.OntrackConnection
import net.nemerosa.ontrack.dsl.http.OTMessageClientException
import org.junit.Before
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AcceptanceTestClient extends AcceptanceSupport {

    private final Logger logger = LoggerFactory.getLogger(AcceptanceTestClient)

    protected Ontrack ontrack

    @Before
    void init() {
        ontrack = ontrackAsAdmin
    }

    protected Ontrack getOntrackAsAdmin() {
        return getOntrackAs('admin', adminPassword)
    }

    protected Ontrack getOntrackAs(String user, String password) {
        return ontrackBuilder
                .authenticate(user, password)
                .build()
    }

    protected OntrackConnection getOntrackBuilder() {
        return OntrackConnection.create(baseURL).disableSsl(sslDisabled)
    }

    protected static File getImageFile() {
        def file = File.createTempFile('image', '.png')
        file.bytes = AbstractACCDSL.class.getResource('/gold.png').bytes
        file
    }

    protected static def validationError(String expectedMessage, Closure code) {
        try {
            code()
            assert false: "Should have failed with: ${expectedMessage}"
        } catch (OTMessageClientException ex) {
            assert ex.message == expectedMessage
        }
    }

    JsonNode doCreateProject() {
        doCreateProject(nameDescription())
    }

    JsonNode doCreateProject(JsonNode nameDescription) {
        admin().post(nameDescription, "structure/projects/create").get()
    }

    def doDeleteProject(String name) {
        logger.debug "Deleting project ${name}"
        def project = admin().get("structure/entity/project/$name").get()
        def link = project._delete.asText()
        logger.debug "Deleting project at ${link}"
        admin().delete(link).get()
    }

    JsonNode doCreateBranch() {
        JsonNode project = doCreateProject()
        doCreateBranch(project.path('id').asInt(), nameDescription())
    }

    JsonNode doCreateBranch(int projectId, JsonNode nameDescription) {
        admin().post(nameDescription, "structure/projects/$projectId/branches/create").get()
    }

    JsonNode doCreateBuild() {
        JsonNode branch = doCreateBranch()
        doCreateBuild(branch.path('id').asInt(), nameDescription())
    }

    JsonNode doCreateBuild(int branchId, JsonNode nameDescription) {
        admin().post(nameDescription, "structure/branches/$branchId/builds/create").get()
    }

    int doCreateController(String name, String password) {
        doCreateAccountWithGlobalRole(name, password, 'CONTROLLER')
    }

    int doCreateAutomation(String name, String password) {
        doCreateAccountWithGlobalRole(name, password, 'AUTOMATION')
    }

    int doCreateCreator(String name, String password) {
        doCreateAccountWithGlobalRole(name, password, 'CREATOR')
    }

    int doCreateAccountWithGlobalRole(String name, String password, String role) {
        def input = [
                name    : name,
                fullName: name,
                email   : "${name}@test.com".toString(),
                password: password,
        ]
        def account = admin().post(input, "accounts/create").get()
        def accountId = account['id'].asText() as int
        admin().put([role: role], "accounts/permissions/globals/ACCOUNT/${accountId}")
        return accountId
    }

    def withProject(Closure closure) {
        def p = doCreateProject()
        int id = p.id.asInt()
        String name = p.name.asText()
        try {
            closure(id, name)
        } finally {
            doDeleteProject name
        }
    }
}
