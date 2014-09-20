package net.nemerosa.ontrack.service;

import net.nemerosa.ontrack.extension.general.LinkProperty;
import net.nemerosa.ontrack.extension.general.LinkPropertyType;
import net.nemerosa.ontrack.model.buildfilter.BuildFilterService;
import net.nemerosa.ontrack.model.security.SecurityService;
import net.nemerosa.ontrack.model.structure.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static net.nemerosa.ontrack.json.JsonUtils.array;
import static net.nemerosa.ontrack.json.JsonUtils.object;
import static net.nemerosa.ontrack.model.structure.NameDescription.nd;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

// TODO Checks the copy of the user filters
public class CopyServiceImplTest {

    private CopyServiceImpl service;
    private StructureService structureService;
    private PropertyService propertyService;

    @Before
    public void before() {
        structureService = mock(StructureService.class);
        propertyService = mock(PropertyService.class);
        SecurityService securityService = mock(SecurityService.class);
        BuildFilterService buildFilterService = mock(BuildFilterService.class);
        service = new CopyServiceImpl(structureService, propertyService, securityService, buildFilterService);
    }

    @Test
    public void applyReplacements_none() {
        assertEquals("branches/11.7", CopyServiceImpl.applyReplacements("branches/11.7", Collections.emptyList()));
    }

    @Test
    public void applyReplacements_direct() {
        assertEquals("branches/11.8", CopyServiceImpl.applyReplacements("branches/11.7", Arrays.asList(
                new Replacement("11.7", "11.8")
        )));
    }

    @Test
    public void applyReplacements_several() {
        assertEquals("Release pipeline for branches/11.7", CopyServiceImpl.applyReplacements("Pipeline for trunk", Arrays.asList(
                new Replacement("trunk", "branches/11.7"),
                new Replacement("Pipeline", "Release pipeline")
        )));
    }

    @Test
    public void doCopyBranchProperties() {
        Branch sourceBranch = Branch.of(Project.of(nd("P1", "")).withId(ID.of(1)), nd("B1", "")).withId(ID.of(1));
        Branch targetBranch = Branch.of(Project.of(nd("P2", "")).withId(ID.of(2)), nd("B2", "")).withId(ID.of(2));
        // Request
        BranchCopyRequest request = new BranchCopyRequest(
                ID.of(1),
                Arrays.asList(
                        new Replacement("P1", "P2")
                ),
                Collections.emptyList(),
                Collections.emptyList()
        );

        // Properties for the branch
        when(propertyService.getProperties(sourceBranch)).thenReturn(
                Arrays.asList(
                        Property.of(
                                new LinkPropertyType(),
                                LinkProperty.of("test", "http://wiki/P1")
                        )
                )
        );

        // Copy
        service.doCopy(sourceBranch, targetBranch, request);

        // Checks the copy of properties for the branch
        verify(propertyService, times(1)).editProperty(
                eq(targetBranch),
                eq(LinkPropertyType.class.getName()),
                eq(object()
                        .with("links", array()
                                .with(object()
                                        .with("name", "test")
                                        .with("value", "http://wiki/P2")
                                        .end())
                                .end())
                        .end())
        );
    }

    @Test
    public void doCopyPromotionLevels() {
        Branch sourceBranch = Branch.of(Project.of(nd("P1", "")).withId(ID.of(1)), nd("B1", "")).withId(ID.of(1));
        Branch targetBranch = Branch.of(Project.of(nd("P2", "")).withId(ID.of(2)), nd("B2", "")).withId(ID.of(2));
        // Request
        BranchCopyRequest request = new BranchCopyRequest(
                ID.of(1),
                Collections.emptyList(),
                Arrays.asList(
                        new Replacement("P1", "P2")
                ),
                Collections.emptyList()
        );
        // Promotion levels for source
        PromotionLevel sourcePromotionLevel = PromotionLevel.of(sourceBranch, nd("copper", "Copper level for P1"));
        when(structureService.getPromotionLevelListForBranch(ID.of(1))).thenReturn(
                Arrays.asList(
                        sourcePromotionLevel
                )
        );
        when(structureService.findPromotionLevelByName("P2", "B2", "copper")).thenReturn(Optional.empty());

        // Promotion level supposed to be created for the target branch
        PromotionLevel targetPromotionLevel = PromotionLevel.of(
                targetBranch,
                nd("copper", "Copper level for P2")
        );
        // Result of the creation
        when(structureService.newPromotionLevel(targetPromotionLevel)).thenReturn(targetPromotionLevel);

        // Properties for the promotion level
        when(propertyService.getProperties(sourcePromotionLevel)).thenReturn(
                Arrays.asList(
                        Property.of(
                                new LinkPropertyType(),
                                LinkProperty.of("test", "http://wiki/P1")
                        )
                )
        );

        // Copy
        service.doCopyPromotionLevels(sourceBranch, targetBranch, request);

        // Checks the promotion level was created
        verify(structureService, times(1)).newPromotionLevel(targetPromotionLevel);
        // Checks the copy of properties for the promotion levels
        verify(propertyService, times(1)).editProperty(
                eq(targetPromotionLevel),
                eq(LinkPropertyType.class.getName()),
                eq(object()
                        .with("links", array()
                                .with(object()
                                        .with("name", "test")
                                        .with("value", "http://wiki/P2")
                                        .end())
                                .end())
                        .end())
        );
    }

    @Test
    public void doCopyValidationStamps() {
        Branch sourceBranch = Branch.of(Project.of(nd("P1", "")).withId(ID.of(1)), nd("B1", "")).withId(ID.of(1));
        Branch targetBranch = Branch.of(Project.of(nd("P2", "")).withId(ID.of(2)), nd("B2", "")).withId(ID.of(2));
        // Request
        BranchCopyRequest request = new BranchCopyRequest(
                ID.of(1),
                Collections.emptyList(),
                Collections.emptyList(),
                Arrays.asList(
                        new Replacement("P1", "P2")
                )
        );
        // Validation stamps for source
        ValidationStamp sourceValidationStamp = ValidationStamp.of(sourceBranch, nd("smoke", "Smoke test for P1"));
        when(structureService.getValidationStampListForBranch(ID.of(1))).thenReturn(
                Arrays.asList(
                        sourceValidationStamp
                )
        );
        when(structureService.findValidationStampByName("P2", "B2", "smoke")).thenReturn(Optional.empty());

        // Validation stamp supposed to be created for the target branch
        ValidationStamp targetValidationStamp = ValidationStamp.of(
                targetBranch,
                nd("smoke", "Smoke test for P2")
        );
        // Result of the creation
        when(structureService.newValidationStamp(targetValidationStamp)).thenReturn(targetValidationStamp);

        // Properties for the validation stamp
        when(propertyService.getProperties(sourceValidationStamp)).thenReturn(
                Arrays.asList(
                        Property.of(
                                new LinkPropertyType(),
                                LinkProperty.of("test", "http://wiki/P1")
                        )
                )
        );

        // Copy
        service.doCopyValidationStamps(sourceBranch, targetBranch, request);

        // Checks the validation stamp was created
        verify(structureService, times(1)).newValidationStamp(targetValidationStamp);
        // Checks the copy of properties for the validation stamps
        verify(propertyService, times(1)).editProperty(
                eq(targetValidationStamp),
                eq(LinkPropertyType.class.getName()),
                eq(object()
                        .with("links", array()
                                .with(object()
                                        .with("name", "test")
                                        .with("value", "http://wiki/P2")
                                        .end())
                                .end())
                        .end())
        );
    }
}