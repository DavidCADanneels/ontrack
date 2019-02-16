package net.nemerosa.ontrack.graphql

import net.nemerosa.ontrack.json.toJson
import net.nemerosa.ontrack.test.TestUtils
import org.junit.Test

class ProjectPackageGraphQLIT : AbstractQLKTITSupport() {

    @Test
    fun `Project package ids`() {
        project {
            packageIds {
                test("one")
                test("two")
            }
            val data = run("""{
                projects(id: ${this.id}) {
                    packageIds {
                        type {
                            id
                            name
                        }
                        id
                    }
                }
            }""")
            val project = data["projects"][0]
            val packages = project["packageIds"]
            TestUtils.assertJsonEquals(
                    listOf(
                            mapOf(
                                    "type" to mapOf(
                                            "id" to "net.nemerosa.ontrack.it.TestPackageType",
                                            "name" to "Test"
                                    ),
                                    "id" to "one"
                            ),
                            mapOf(
                                    "type" to mapOf(
                                            "id" to "net.nemerosa.ontrack.it.TestPackageType",
                                            "name" to "Test"
                                    ),
                                    "id" to "two"
                            )
                    ).toJson(),
                    packages
            )
        }
    }

}