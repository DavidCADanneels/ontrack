package net.nemerosa.ontrack.graphql.schema;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLFieldDefinition;
import net.nemerosa.ontrack.graphql.support.GraphqlUtils;
import net.nemerosa.ontrack.model.security.AccountService;
import net.nemerosa.ontrack.model.security.ProjectRole;
import net.nemerosa.ontrack.model.security.RolesService;
import net.nemerosa.ontrack.model.structure.Project;
import net.nemerosa.ontrack.model.structure.ProjectEntity;
import net.nemerosa.ontrack.model.structure.ProjectEntityType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static graphql.Scalars.GraphQLString;
import static net.nemerosa.ontrack.graphql.support.GraphqlUtils.fetcher;
import static net.nemerosa.ontrack.graphql.support.GraphqlUtils.stdList;

@Component
public class GQLProjectAuthorizationsFieldContributor implements GQLProjectEntityFieldContributor {

    private final AccountService accountService;
    private final RolesService rolesService;
    private final GQLTypeProjectAuthorization projectAuthorization;

    @Autowired
    public GQLProjectAuthorizationsFieldContributor(
            AccountService accountService,
            RolesService rolesService,
            GQLTypeProjectAuthorization projectAuthorization
    ) {
        this.accountService = accountService;
        this.rolesService = rolesService;
        this.projectAuthorization = projectAuthorization;
    }

    @Override
    public List<GraphQLFieldDefinition> getFields(
            Class<? extends ProjectEntity> projectEntityClass,
            ProjectEntityType projectEntityType) {
        if (projectEntityType == ProjectEntityType.PROJECT) {
            return Collections.singletonList(
                    GraphQLFieldDefinition.newFieldDefinition()
                            .name("projectRoles")
                            .description("Authorisations for the project")
                            .type(stdList(projectAuthorization.getTypeRef()))
                            .argument(a -> a.name("role")
                                    .description("Filter by role name")
                                    .type(GraphQLString)
                            )
                            .dataFetcher(projectAuthorizationsFetcher())
                            .build()
            );
        } else {
            return Collections.emptyList();
        }
    }

    private DataFetcher projectAuthorizationsFetcher() {
        return fetcher(
                Project.class,
                (environment, project) -> rolesService.getProjectRoles().stream()
                        .filter(GraphqlUtils.getStringArgument(environment, "role")
                                .map(s -> (Predicate<ProjectRole>) pr -> StringUtils.equals(s, pr.getId()))
                                .orElseGet(() -> pr -> true))
                        .map(projectRole -> getProjectAuthorizations(project, projectRole))
                        .collect(Collectors.toList())
        );
    }

    private GQLTypeProjectAuthorization.Model getProjectAuthorizations(Project project, ProjectRole projectRole) {
        return new GQLTypeProjectAuthorization.Model(
                projectRole.getId(),
                projectRole.getName(),
                projectRole.getDescription(),
                accountService.findAccountGroupsByProjectRole(project, projectRole),
                accountService.findAccountsByProjectRole(project, projectRole)
        );
    }
}
