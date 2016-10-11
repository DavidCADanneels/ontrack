package net.nemerosa.ontrack.graphql;

import graphql.schema.*;
import net.nemerosa.ontrack.model.structure.StructureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

@Configuration
public class GraphqlConfig {

    public static final String QUERY = "Query";
    public static final String PROJECT = "Project";

    @Autowired
    private StructureService structureService;

    /**
     * GraphQL schema definition
     */
    @Bean
    @Qualifier("ontrack")
    public GraphQLSchema grapqlSchema() {
        return GraphQLSchema.newSchema()
                .query(queryType())
                .build();
    }

    private GraphQLObjectType projectType() {
        return newObject()
                .name(PROJECT)
                .field(
                        newFieldDefinition()
                                .name("name")
                                .type(new GraphQLNonNull(GraphQLString))
                                .build()
                )
                // TODO Branches
                // TODO Project properties
                // OK
                .build();
    }

    private GraphQLObjectType queryType() {
        return newObject()
                .name(QUERY)
                // Project
                .field(
                        newFieldDefinition()
                                .name("project")
                                .type(new GraphQLList(projectType()))
                                .dataFetcher(projectFetcher())
                                .build()
                )
                // TODO Extension contributions
                // OK
                .build();
    }

    private DataFetcher projectFetcher() {
        // TODO Search criterias
        return environment -> structureService.getProjectList();
    }

}
