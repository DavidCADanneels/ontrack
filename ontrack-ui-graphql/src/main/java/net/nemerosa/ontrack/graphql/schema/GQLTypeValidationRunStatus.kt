package net.nemerosa.ontrack.graphql.schema

import graphql.schema.GraphQLFieldDefinition.newFieldDefinition
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLObjectType.newObject
import net.nemerosa.ontrack.graphql.support.GraphqlUtils
import net.nemerosa.ontrack.model.structure.ValidationRunStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class GQLTypeValidationRunStatus
@Autowired
constructor(
        private val validationRunStatusID: GQLTypeValidationRunStatusID,
        private val creation: GQLTypeCreation
) : GQLType {

    override fun getType(): GraphQLObjectType {
        return newObject()
                .name(VALIDATION_RUN_STATUS)
                // Creation
                .field {
                    it.name("creation")
                            .type(creation.type)
                            .dataFetcher(GQLTypeCreation.dataFetcher<ValidationRunStatus> { it.signature })
                }
                // Status ID
                .field(
                        newFieldDefinition()
                                .name("statusID")
                                .description("Status ID")
                                .type(validationRunStatusID.type)
                                .build()
                )
                // Description
                .field(GraphqlUtils.descriptionField())
                // OK
                .build()

    }

    companion object {

        val VALIDATION_RUN_STATUS = "ValidationRunStatus"
    }

}
