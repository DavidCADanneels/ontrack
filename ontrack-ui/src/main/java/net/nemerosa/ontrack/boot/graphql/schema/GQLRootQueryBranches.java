package net.nemerosa.ontrack.boot.graphql.schema;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLFieldDefinition;
import net.nemerosa.ontrack.model.structure.Branch;
import net.nemerosa.ontrack.model.structure.ID;
import net.nemerosa.ontrack.model.structure.Project;
import net.nemerosa.ontrack.model.structure.StructureService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static net.nemerosa.ontrack.boot.graphql.support.GraphqlUtils.checkArgList;
import static net.nemerosa.ontrack.boot.graphql.support.GraphqlUtils.stdList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
public class GQLRootQueryBranches implements GQLRootQuery {

    private final StructureService structureService;
    private final GQLTypeBranch branch;

    @Autowired
    public GQLRootQueryBranches(StructureService structureService, GQLTypeBranch branch) {
        this.structureService = structureService;
        this.branch = branch;
    }

    @Override
    public GraphQLFieldDefinition getFieldDefinition() {
        return newFieldDefinition()
                .name("branches")
                .type(stdList(branch.getType()))
                .argument(
                        newArgument()
                                .name("id")
                                .description("ID of the branch to look for")
                                .type(GraphQLInt)
                                .build()
                )
                .argument(
                        newArgument()
                                .name("project")
                                .description("Name of the project the branch belongs to")
                                .type(GraphQLString)
                                .build()
                )
                .argument(
                        newArgument()
                                .name("name")
                                .description("Regular expression to match against the branch name")
                                .type(GraphQLString)
                                .build()
                )
                .dataFetcher(branchFetcher())
                .build();
    }

    private DataFetcher branchFetcher() {
        return environment -> {
            Integer id = environment.getArgument("id");
            String projectName = environment.getArgument("project");
            String name = environment.getArgument("name");
            // Per ID
            if (id != null) {
                checkArgList(environment, "id");
                return Collections.singletonList(
                        structureService.getBranch(ID.of(id))
                );
            }
            // Per project name
            else if (isNotBlank(projectName) || isNotBlank(name)) {

                // Project filter
                Predicate<Project> projectFilter = p -> true;
                if (isNotBlank(projectName)) {
                    projectFilter = projectFilter.and(project -> StringUtils.equals(projectName, project.getName()));
                }

                // Branch filter
                Predicate<Branch> branchFilter = b -> true;
                if (isNotBlank(name)) {
                    Pattern pattern = Pattern.compile(name);
                    branchFilter = branchFilter.and(b -> pattern.matcher(b.getName()).matches());
                }

                // Gets the list of authorised projects
                return structureService.getProjectList().stream()
                        // Filter on the project
                        .filter(projectFilter)
                        // Gets the list of branches
                        .flatMap(project -> structureService.getBranchesForProject(project.getId()).stream())
                        // Filter on the branch
                        .filter(branchFilter)
                        // OK
                        .collect(Collectors.toList());
            }
            // Whole list
            else {
                return Collections.emptyList();
            }
        };
    }

}
