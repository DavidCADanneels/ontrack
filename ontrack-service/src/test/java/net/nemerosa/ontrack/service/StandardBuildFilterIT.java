package net.nemerosa.ontrack.service;

import lombok.Data;
import net.nemerosa.ontrack.it.AbstractServiceTestSupport;
import net.nemerosa.ontrack.model.buildfilter.BuildFilterProviderData;
import net.nemerosa.ontrack.model.buildfilter.BuildFilterService;
import net.nemerosa.ontrack.model.security.BuildCreate;
import net.nemerosa.ontrack.model.security.PromotionRunCreate;
import net.nemerosa.ontrack.model.structure.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class StandardBuildFilterIT extends AbstractServiceTestSupport {

    @Autowired
    private StructureService structureService;
    @Autowired
    private BuildFilterService buildFilterService;

    private Branch branch;
    private PromotionLevel copper;
    private PromotionLevel bronze;

    @Before
    public void prepare() throws Exception {
        branch = doCreateBranch();
        copper = doCreatePromotionLevel(branch, NameDescription.nd("COPPER", ""));
        bronze = doCreatePromotionLevel(branch, NameDescription.nd("BRONZE", ""));
    }

    /**
     * Tests the following sequence:
     * <p>
     * <pre>
     *     1
     *     2
     *     3
     *     4 --> COPPER
     *     5 --> BRONZE
     * </pre>
     * <ul>
     * <li>Since promotion level: COPPER</li>
     * </ul>
     * <p>
     * Builds 5 and 4 must be displayed.
     */
    @Test
    public void since_promotion_level() throws Exception {
        // Builds
        build(1);
        build(2);
        build(3);
        build(4).withPromotion(copper);
        build(5).withPromotion(bronze);
        // Filter
        BuildFilterProviderData<?> filter = buildFilterService.standardFilterProviderData(5)
                .withSincePromotionLevel("COPPER")
                .build();
        // Filtering
        List<Build> builds = filter.filterBranchBuilds(branch.getId());
        // Checks the list
        checkList(builds, 5, 4);
    }

    /**
     * Tests the following sequence:
     * <p>
     * <pre>
     *     1
     *     2 --> COPPER
     *     3
     *     4 --> COPPER
     *     5 --> COPPER, BRONZE
     * </pre>
     * <ul>
     * <li>With promotion level: COPPER</li>
     * <li>Since promotion level: BRONZE</li>
     * </ul>
     * <p>
     * Build 5 should be accepted and no further build should be scan for:
     */
    @Test
    public void with_since_promotion_level() throws Exception {
        // Builds
        build(1);
        build(2).withPromotion(copper);
        build(3);
        build(4).withPromotion(copper);
        build(5).withPromotion(copper).withPromotion(bronze);
        // Filter
        BuildFilterProviderData<?> filter = buildFilterService.standardFilterProviderData(5)
                .withSincePromotionLevel("BRONZE")
                .withWithPromotionLevel("COPPER")
                .build();
        // Filtering
        List<Build> builds = filter.filterBranchBuilds(branch.getId());
        // Checks the list
        checkList(builds, 5);
    }

    protected BuildCreator build(int name) throws Exception {
        return build(String.valueOf(name));
    }

    protected BuildCreator build(String name) throws Exception {
        Build build = asUser().with(branch, BuildCreate.class).call(() ->
                structureService.newBuild(
                        Build.of(
                                branch,
                                new NameDescription(name, "Build " + name),
                                Signature.of("user").withTime(LocalDateTime.of(2014, 7, 14, 13, 25, 0))
                        )
                )
        );
        return new BuildCreator(build);
    }

    @Data
    protected class BuildCreator {

        private final Build build;

        public BuildCreator withPromotion(PromotionLevel promotionLevel) throws Exception {
            asUser().with(branch, PromotionRunCreate.class).call(() ->
                    structureService.newPromotionRun(
                            PromotionRun.of(
                                    build,
                                    promotionLevel,
                                    Signature.of("user"),
                                    ""
                            )
                    )
            );
            return this;
        }
    }

    protected void checkList(List<Build> builds, Integer... ids) {
        List<Integer> expectedIds = Arrays.asList(ids);
        List<Integer> actualIds = builds.stream()
                .map(Entity::id)
                .collect(Collectors.toList());
        assertEquals(expectedIds, actualIds);
    }

}