package net.nemerosa.ontrack.graphql.schema;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeReference;
import net.nemerosa.ontrack.graphql.support.ConnectionList;
import net.nemerosa.ontrack.graphql.support.GraphqlUtils;
import net.nemerosa.ontrack.graphql.support.Relay;
import net.nemerosa.ontrack.model.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

@Component
public class GQLTypeValidationStamp extends AbstractGQLProjectEntity<ValidationStamp> {

    public static final String VALIDATION_STAMP = "ValidationStamp";

    private final StructureService structureService;
    private final GQLTypeValidationRun validationRun;

    @Autowired
    public GQLTypeValidationStamp(StructureService structureService,
                                  GQLTypeValidationRun validationRun,
                                  List<GQLProjectEntityFieldContributor> projectEntityFieldContributors) {
        super(ValidationStamp.class, ProjectEntityType.VALIDATION_STAMP, projectEntityFieldContributors);
        this.structureService = structureService;
        this.validationRun = validationRun;
    }

    @Override
    public GraphQLObjectType getType() {
        return newObject()
                .name(VALIDATION_STAMP)
                .withInterface(projectEntityInterface())
                .fields(projectEntityInterfaceFields())
                // Ref to branch
                .field(
                        newFieldDefinition()
                                .name("branch")
                                .description("Reference to branch")
                                .type(new GraphQLTypeReference(GQLTypeBranch.BRANCH))
                                .build()
                )
                // Validation runs
                .field(
                        newFieldDefinition()
                                .name("validationRuns")
                                .description("List of runs for this validation stamp")
                                .type(GraphqlUtils.connectionList(validationRun.getType()))
                                .argument(Relay.getConnectionFieldArguments())
                                .dataFetcher(validationStampValidationRunsFetcher())
                                .build()
                )
                // OK
                .build();

    }

    private DataFetcher validationStampValidationRunsFetcher() {
        return environment -> {
            Object source = environment.getSource();
            if (source instanceof ValidationStamp) {
                ValidationStamp validationStamp = (ValidationStamp) source;
                // Gets all the validation runs
                List<ValidationRun> validationRuns = structureService.getValidationRunsForValidationStamp(
                        validationStamp.getId(),
                        0,
                        Integer.MAX_VALUE
                );
                // As a connection list
                return new ConnectionList(validationRuns).get(environment);
            } else {
                return Collections.emptyList();
            }
        };
    }

    @Override
    protected Optional<Signature> getSignature(ValidationStamp entity) {
        return Optional.ofNullable(entity.getSignature());
    }

}
