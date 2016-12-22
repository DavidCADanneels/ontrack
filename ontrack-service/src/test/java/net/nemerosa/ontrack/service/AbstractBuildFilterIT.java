package net.nemerosa.ontrack.service;

import lombok.Data;
import net.nemerosa.ontrack.extension.api.support.TestSimpleProperty;
import net.nemerosa.ontrack.extension.api.support.TestSimplePropertyType;
import net.nemerosa.ontrack.it.AbstractServiceTestSupport;
import net.nemerosa.ontrack.model.buildfilter.BuildFilterService;
import net.nemerosa.ontrack.model.security.*;
import net.nemerosa.ontrack.model.structure.*;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.nemerosa.ontrack.model.structure.NameDescription.nd;
import static org.junit.Assert.assertEquals;

public abstract class AbstractBuildFilterIT extends AbstractServiceTestSupport {

    @Autowired
    protected PropertyService propertyService;
    @Autowired
    protected StructureService structureService;
    @Autowired
    protected BuildFilterService buildFilterService;

    protected Branch branch;
    protected PromotionLevel copper;
    protected PromotionLevel bronze;
    protected ValidationStamp publication;
    protected ValidationStamp production;

    @Before
    public void prepare() throws Exception {
        branch = doCreateBranch();
        copper = doCreatePromotionLevel(branch, nd("COPPER", ""));
        bronze = doCreatePromotionLevel(branch, nd("BRONZE", ""));
        publication = doCreateValidationStamp(branch, nd("PUBLICATION", ""));
        production = doCreateValidationStamp(branch, nd("PRODUCTION", ""));
    }

    protected BuildCreator build(int name) throws Exception {
        return build(String.valueOf(name));
    }

    protected BuildCreator build(String name) throws Exception {
        return build(name, LocalDateTime.of(2014, 7, 14, 13, 25, 0));
    }

    protected BuildCreator build(int name, LocalDateTime dateTime) throws Exception {
        return build(String.valueOf(name), dateTime);
    }

    protected BuildCreator build(String name, LocalDateTime dateTime) throws Exception {
        Build build = asUser().with(branch, BuildCreate.class).call(() ->
                structureService.newBuild(
                        Build.of(
                                branch,
                                new NameDescription(name, "Build " + name),
                                Signature.of("user").withTime(dateTime)
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

        public BuildCreator withValidation(ValidationStamp stamp, ValidationRunStatusID status) throws Exception {
            asUser().with(branch, ValidationRunCreate.class).call(() ->
                    structureService.newValidationRun(
                            ValidationRun.of(
                                    build,
                                    stamp,
                                    1,
                                    Signature.of("user"),
                                    status,
                                    ""
                            )
                    )
            );
            return this;
        }

        public BuildCreator linkedFrom(Build otherBuild) throws Exception {
            asUser()
                    .with(branch, ProjectView.class)
                    .with(otherBuild, BuildEdit.class)
                    .call(() -> {
                        structureService.addBuildLink(
                                otherBuild,
                                build
                        );
                        return null;
                    });
            return this;
        }

        public BuildCreator linkedTo(Build otherBuild) throws Exception {
            asUser()
                    .with(branch, BuildEdit.class)
                    .with(otherBuild, ProjectView.class)
                    .call(() -> {
                        structureService.addBuildLink(
                                build,
                                otherBuild
                        );
                        return null;
                    });
            return this;
        }

        public BuildCreator withProperty(String value) throws Exception {
            asUser().with(build, ProjectEdit.class).call(() ->
                    propertyService.editProperty(
                            build,
                            TestSimplePropertyType.class,
                            new TestSimpleProperty(value)
                    )
            );
            return this;
        }
    }

    protected void checkList(List<Build> builds, Integer... names) {
        List<String> expectedNames = Arrays.stream(names)
                .map(String::valueOf)
                .collect(Collectors.toList());
        List<String> actualNames = builds.stream()
                .map(Build::getName)
                .collect(Collectors.toList());
        assertEquals(expectedNames, actualNames);
    }

}