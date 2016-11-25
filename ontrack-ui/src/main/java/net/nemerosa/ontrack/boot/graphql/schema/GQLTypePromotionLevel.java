package net.nemerosa.ontrack.boot.graphql.schema;

import graphql.relay.SimpleListConnection;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLObjectType;
import net.nemerosa.ontrack.boot.graphql.support.GraphqlUtils;
import net.nemerosa.ontrack.boot.graphql.support.Relay;
import net.nemerosa.ontrack.model.events.EventFactory;
import net.nemerosa.ontrack.model.events.EventQueryService;
import net.nemerosa.ontrack.model.events.EventType;
import net.nemerosa.ontrack.model.security.SecurityService;
import net.nemerosa.ontrack.model.structure.PromotionLevel;
import net.nemerosa.ontrack.model.structure.PromotionRun;
import net.nemerosa.ontrack.model.structure.StructureService;
import net.nemerosa.ontrack.ui.controller.URIBuilder;
import net.nemerosa.ontrack.ui.resource.ResourceDecorator;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

@Component
public class GQLTypePromotionLevel extends AbstractGQLProjectEntityWithoutSignature<PromotionLevel> {

    public static final String PROMOTION_LEVEL = "PromotionLevel";

    private final StructureService structureService;
    private final GQLTypePromotionRun promotionRun;

    public GQLTypePromotionLevel(URIBuilder uriBuilder, SecurityService securityService, List<ResourceDecorator<?>> decorators, StructureService structureService, GQLTypePromotionRun promotionRun, EventQueryService eventQueryService) {
        super(uriBuilder, securityService, PromotionLevel.class, decorators, eventQueryService);
        this.structureService = structureService;
        this.promotionRun = promotionRun;
    }

    @Override
    public GraphQLObjectType getType() {
        return newObject()
                .name(PROMOTION_LEVEL)
                .withInterface(projectEntityInterface())
                .fields(projectEntityInterfaceFields())
                // TODO Image
                // Promotion runs
                .field(
                        newFieldDefinition()
                                .name("promotionRuns")
                                .description("List of runs for this promotion")
                                .type(GraphqlUtils.connectionList(promotionRun.getType()))
                                .argument(Relay.getConnectionFieldArguments())
                                .dataFetcher(promotionLevelPromotionRunsFetcher())
                                .build()
                )
                // OK
                .build();
    }

    private DataFetcher promotionLevelPromotionRunsFetcher() {
        return environment -> {
            Object source = environment.getSource();
            if (source instanceof PromotionLevel) {
                PromotionLevel promotionLevel = (PromotionLevel) source;
                // Gets all the promotion runs
                List<PromotionRun> promotionRuns = structureService.getPromotionRunsForPromotionLevel(promotionLevel.getId());
                // As a connection list
                return new SimpleListConnection(promotionRuns).get(environment);
            } else {
                return Collections.emptyList();
            }
        };
    }

    @Override
    protected EventType getEventCreationType() {
        return EventFactory.NEW_PROMOTION_LEVEL;
    }
}
