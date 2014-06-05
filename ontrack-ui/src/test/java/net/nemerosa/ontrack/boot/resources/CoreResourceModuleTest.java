package net.nemerosa.ontrack.boot.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import net.nemerosa.ontrack.model.security.ProjectEdit;
import net.nemerosa.ontrack.model.security.SecurityService;
import net.nemerosa.ontrack.model.structure.*;
import net.nemerosa.ontrack.ui.controller.MockURIBuilder;
import net.nemerosa.ontrack.ui.resource.DefaultResourceContext;
import net.nemerosa.ontrack.ui.resource.ResourceObjectMapper;
import net.nemerosa.ontrack.ui.resource.ResourceObjectMapperFactory;
import net.nemerosa.ontrack.ui.resource.Resources;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static net.nemerosa.ontrack.json.JsonUtils.array;
import static net.nemerosa.ontrack.json.JsonUtils.object;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class CoreResourceModuleTest {

    private ResourceObjectMapper mapper;
    private SecurityService securityService;

    @Before
    public void before() {
        securityService = Mockito.mock(SecurityService.class);
        mapper = new ResourceObjectMapperFactory().resourceObjectMapper(
                Arrays.asList(
                        new CoreResourceModule()
                ),
                new DefaultResourceContext(new MockURIBuilder(), securityService)
        );
    }

    public static void assertResourceJson(ResourceObjectMapper mapper, JsonNode expectedJson, Object o) throws JsonProcessingException {
        assertEquals(
                mapper.getObjectMapper().writeValueAsString(expectedJson),
                mapper.write(o)
        );
    }

    public static void assertResourceJson(ResourceObjectMapper mapper, JsonNode expectedJson, Object o, Class<?> view) throws JsonProcessingException {
        assertEquals(
                mapper.getObjectMapper().writeValueAsString(expectedJson),
                mapper.write(o, view)
        );
    }

    @Test
    public void project_granted_for_update() throws JsonProcessingException {
        Project p = Project.of(new NameDescription("P", "Project")).withId(ID.of(1));
        when(securityService.isProjectFunctionGranted(1, ProjectEdit.class)).thenReturn(true);
        assertResourceJson(
                mapper,
                object()
                        .with("id", 1)
                        .with("name", "P")
                        .with("description", "Project")
                        .with("_self", "urn:test:net.nemerosa.ontrack.boot.ui.ProjectController#getProject:1")
                        .with("_branches", "urn:test:net.nemerosa.ontrack.boot.ui.BranchController#getBranchListForProject:1")
                        .with("_editableProperties", "urn:test:net.nemerosa.ontrack.boot.ui.PropertyController#getEditableProperties:PROJECT,1")
                        .with("_properties", "urn:test:net.nemerosa.ontrack.boot.ui.PropertyController#getProperties:PROJECT,1")
                        .with("_update", "urn:test:net.nemerosa.ontrack.boot.ui.ProjectController#saveProject:1,")
                        .end(),
                p
        );
    }

    @Test
    public void branch_no_grant() throws JsonProcessingException {
        // Objects
        Project p = Project.of(new NameDescription("P", "Project")).withId(ID.of(1));
        Branch b = Branch.of(p, new NameDescription("B", "Branch")).withId(ID.of(1));
        // Serialization
        assertResourceJson(
                mapper,
                object()
                        .with("id", 1)
                        .with("name", "B")
                        .with("description", "Branch")
                        .with("project", object()
                                        .with("id", 1)
                                        .with("name", "P")
                                        .with("description", "Project")
                                        .with("_self", "urn:test:net.nemerosa.ontrack.boot.ui.ProjectController#getProject:1")
                                        .with("_branches", "urn:test:net.nemerosa.ontrack.boot.ui.BranchController#getBranchListForProject:1")
                                        .with("_editableProperties", "urn:test:net.nemerosa.ontrack.boot.ui.PropertyController#getEditableProperties:PROJECT,1")
                                        .with("_properties", "urn:test:net.nemerosa.ontrack.boot.ui.PropertyController#getProperties:PROJECT,1")
                                        .end()
                        )
                        .with("_self", "urn:test:net.nemerosa.ontrack.boot.ui.BranchController#getBranch:1")
                        .with("_project", "urn:test:net.nemerosa.ontrack.boot.ui.ProjectController#getProject:1")
                        .with("_promotionLevels", "urn:test:net.nemerosa.ontrack.boot.ui.PromotionLevelController#getPromotionLevelListForBranch:1")
                        .with("_validationStamps", "urn:test:net.nemerosa.ontrack.boot.ui.ValidationStampController#getValidationStampListForBranch:1")
                        .end(),
                b
        );
    }

    @Test
    public void project_not_granted_for_update() throws JsonProcessingException {
        Project p = Project.of(new NameDescription("P", "Project")).withId(ID.of(1));
        assertResourceJson(
                mapper,
                object()
                        .with("id", 1)
                        .with("name", "P")
                        .with("description", "Project")
                        .with("_self", "urn:test:net.nemerosa.ontrack.boot.ui.ProjectController#getProject:1")
                        .with("_branches", "urn:test:net.nemerosa.ontrack.boot.ui.BranchController#getBranchListForProject:1")
                        .with("_editableProperties", "urn:test:net.nemerosa.ontrack.boot.ui.PropertyController#getEditableProperties:PROJECT,1")
                        .with("_properties", "urn:test:net.nemerosa.ontrack.boot.ui.PropertyController#getProperties:PROJECT,1")
                        .end(),
                p
        );
    }

    @Test
    public void promotion_level_image_link_and_ignored_branch() throws JsonProcessingException {
        // Objects
        Project p = Project.of(new NameDescription("P", "Project")).withId(ID.of(1));
        Branch b = Branch.of(p, new NameDescription("B", "Branch")).withId(ID.of(1));
        PromotionLevel pl = PromotionLevel.of(b, new NameDescription("PL", "Promotion level")).withId(ID.of(1));
        // Serialization
        assertResourceJson(
                mapper,
                object()
                        .with("id", 1)
                        .with("name", "PL")
                        .with("description", "Promotion level")
                        .with("image", false)
                        .with("_self", "urn:test:net.nemerosa.ontrack.boot.ui.PromotionLevelController#getPromotionLevel:1")
                        .with("_branch", "urn:test:net.nemerosa.ontrack.boot.ui.BranchController#getBranch:1")
                        .with("_project", "urn:test:net.nemerosa.ontrack.boot.ui.ProjectController#getProject:1")
                        .with("_image", "urn:test:net.nemerosa.ontrack.boot.ui.PromotionLevelController#getPromotionLevelImage_:1")
                        .end(),
                pl,
                Branch.class
        );
    }

    @Test
    public void promotion_level_image_link_and_include_branch() throws JsonProcessingException {
        // Objects
        Project p = Project.of(new NameDescription("P", "Project")).withId(ID.of(1));
        Branch b = Branch.of(p, new NameDescription("B", "Branch")).withId(ID.of(1));
        PromotionLevel pl = PromotionLevel.of(b, new NameDescription("PL", "Promotion level")).withId(ID.of(1));
        // Serialization
        assertResourceJson(
                mapper,
                object()
                        .with("id", 1)
                        .with("name", "PL")
                        .with("description", "Promotion level")
                        .with("image", false)
                        .with("branch", object()
                                .with("id", 1)
                                .with("name", "B")
                                .with("description", "Branch")
                                .with("project", object()
                                        .with("id", 1)
                                        .with("name", "P")
                                        .with("description", "Project")
                                        .with("_self", "urn:test:net.nemerosa.ontrack.boot.ui.ProjectController#getProject:1")
                                        .with("_branches", "urn:test:net.nemerosa.ontrack.boot.ui.BranchController#getBranchListForProject:1")
                                        .with("_editableProperties", "urn:test:net.nemerosa.ontrack.boot.ui.PropertyController#getEditableProperties:PROJECT,1")
                                        .with("_properties", "urn:test:net.nemerosa.ontrack.boot.ui.PropertyController#getProperties:PROJECT,1")
                                        .end())
                                .with("_self", "urn:test:net.nemerosa.ontrack.boot.ui.BranchController#getBranch:1")
                                .with("_project", "urn:test:net.nemerosa.ontrack.boot.ui.ProjectController#getProject:1")
                                .with("_promotionLevels", "urn:test:net.nemerosa.ontrack.boot.ui.PromotionLevelController#getPromotionLevelListForBranch:1")
                                .with("_validationStamps", "urn:test:net.nemerosa.ontrack.boot.ui.ValidationStampController#getValidationStampListForBranch:1")
                                .end())
                        .with("_self", "urn:test:net.nemerosa.ontrack.boot.ui.PromotionLevelController#getPromotionLevel:1")
                        .with("_branch", "urn:test:net.nemerosa.ontrack.boot.ui.BranchController#getBranch:1")
                        .with("_project", "urn:test:net.nemerosa.ontrack.boot.ui.ProjectController#getProject:1")
                        .with("_image", "urn:test:net.nemerosa.ontrack.boot.ui.PromotionLevelController#getPromotionLevelImage_:1")
                        .end(),
                pl
        );
    }

    @Test
    public void resource_collection_with_filtering() throws JsonProcessingException {
        Project project = Project.of(new NameDescription("PRJ", "Project")).withId(ID.of(1));
        List<Branch> branches = Arrays.asList(
                Branch.of(project, new NameDescription("B1", "Branch 1")).withId(ID.of(1)),
                Branch.of(project, new NameDescription("B2", "Branch 2")).withId(ID.of(2))
        );
        Resources<Branch> resourceCollection = Resources.of(
                branches,
                URI.create("urn:branch")
        );

        assertResourceJson(
                mapper,
                object()
                        .with("_self", "urn:branch")
                        .with("resources", array()
                                .with(object()
                                        .with("id", 1)
                                        .with("name", "B1")
                                        .with("description", "Branch 1")
                                        .with("_self", "urn:test:net.nemerosa.ontrack.boot.ui.BranchController#getBranch:1")
                                        .with("_project", "urn:test:net.nemerosa.ontrack.boot.ui.ProjectController#getProject:1")
                                        .with("_promotionLevels", "urn:test:net.nemerosa.ontrack.boot.ui.PromotionLevelController#getPromotionLevelListForBranch:1")
                                        .with("_validationStamps", "urn:test:net.nemerosa.ontrack.boot.ui.ValidationStampController#getValidationStampListForBranch:1")
                                        .end())
                                .with(object()
                                        .with("id", 2)
                                        .with("name", "B2")
                                        .with("description", "Branch 2")
                                        .with("_self", "urn:test:net.nemerosa.ontrack.boot.ui.BranchController#getBranch:2")
                                        .with("_project", "urn:test:net.nemerosa.ontrack.boot.ui.ProjectController#getProject:1")
                                        .with("_promotionLevels", "urn:test:net.nemerosa.ontrack.boot.ui.PromotionLevelController#getPromotionLevelListForBranch:2")
                                        .with("_validationStamps", "urn:test:net.nemerosa.ontrack.boot.ui.ValidationStampController#getValidationStampListForBranch:2")
                                        .end())
                                .end())
                        .end(),
                resourceCollection
        );
    }

}
