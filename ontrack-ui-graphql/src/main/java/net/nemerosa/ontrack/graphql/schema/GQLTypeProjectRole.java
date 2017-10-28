package net.nemerosa.ontrack.graphql.schema;

import graphql.schema.GraphQLObjectType;
import net.nemerosa.ontrack.graphql.support.GraphqlUtils;
import org.springframework.stereotype.Component;

import static graphql.schema.GraphQLObjectType.newObject;

/**
 * @see net.nemerosa.ontrack.model.security.ProjectRole
 */
@Component
public class GQLTypeProjectRole implements GQLType {

    public static final String PROJECT_ROLE = "ProjectRole";

    @Override
    public String getTypeName() {
        return PROJECT_ROLE;
    }

    @Override
    public GraphQLObjectType createType(GQLTypeCache cache) {
        return newObject()
                .name(PROJECT_ROLE)
                .field(GraphqlUtils.stringField("id", "ID of the role"))
                .field(GraphqlUtils.nameField("Unique name for the role"))
                .field(GraphqlUtils.stringField("description", "Description of the role"))
                .build();
    }

}
