package net.nemerosa.ontrack.extension.git.graphql

import graphql.schema.GraphQLObjectType
import net.nemerosa.ontrack.extension.git.model.OntrackGitIssueInfo
import net.nemerosa.ontrack.graphql.schema.GQLType
import net.nemerosa.ontrack.graphql.schema.GQLTypeCache
import org.springframework.stereotype.Component

/**
 * GraphQL type for [OntrackGitIssueInfo].
 */
@Component
class OntrackGitIssueInfoGQLType(
        private val ontrackGitCommitInfoGQLType: OntrackGitCommitInfoGQLType,
        private val issueServiceConfigurationRepresentationGQLType: IssueServiceConfigurationRepresentationGQLType
) : GQLType {
    override fun getTypeName(): String =
            OntrackGitIssueInfo::class.java.simpleName

    override fun createType(cache: GQLTypeCache): GraphQLObjectType =
            GraphQLObjectType.newObject()
                    .name(typeName)
                    .description("Information about an issue")
                    // Issue service configuration representation
                    .field {
                        it.name("issueServiceConfigurationRepresentation")
                                .description("Issue service associated with the issue")
                                .type(issueServiceConfigurationRepresentationGQLType.typeRef)
                    }
                    // TODO Issue
                    // Commit info
                    .field {
                        it.name("commitInfo")
                                .description("Information about the last commit associated with this issue")
                                .type(ontrackGitCommitInfoGQLType.typeRef)
                    }
                    // OK
                    .build()
}