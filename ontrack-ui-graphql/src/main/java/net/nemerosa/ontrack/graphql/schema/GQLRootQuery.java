package net.nemerosa.ontrack.graphql.schema;

import graphql.schema.GraphQLFieldDefinition;

// TODO Promotion levels
// TODO Promotion runs
// TODO Validation stamps

/**
 * Provides a root query
 */
public interface GQLRootQuery {

    /**
     * Field definition to use as a field in the root query.
     */
    GraphQLFieldDefinition getFieldDefinition();

}
