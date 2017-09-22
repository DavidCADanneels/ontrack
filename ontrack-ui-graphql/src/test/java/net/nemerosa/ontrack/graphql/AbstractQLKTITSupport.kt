package net.nemerosa.ontrack.graphql

import com.fasterxml.jackson.databind.JsonNode
import graphql.GraphQL
import net.nemerosa.ontrack.graphql.schema.GraphqlSchemaService
import net.nemerosa.ontrack.it.AbstractServiceTestSupport
import net.nemerosa.ontrack.json.JsonUtils
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.fail

abstract class AbstractQLKTITSupport : AbstractServiceTestSupport() {

    @Autowired
    private lateinit var schemaService: GraphqlSchemaService

    fun run(query: String): JsonNode {
        val result = GraphQL(schemaService.schema).execute(query)
        if (result.errors != null && !result.errors.isEmpty()) {
            fail(result.errors.joinToString("\n") { it.message })
        } else if (result.data != null) {
            return JsonUtils.format(result.data)
        } else {
            fail("No data was returned and no error was thrown.")
        }
    }

}
