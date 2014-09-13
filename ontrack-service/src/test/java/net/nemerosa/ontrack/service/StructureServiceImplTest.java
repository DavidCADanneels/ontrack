package net.nemerosa.ontrack.service;

import net.nemerosa.ontrack.model.security.SecurityService;
import net.nemerosa.ontrack.model.structure.*;
import net.nemerosa.ontrack.repository.StructureRepository;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import static net.nemerosa.ontrack.model.structure.NameDescription.nd;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class StructureServiceImplTest {

    private StructureServiceImpl service;
    private StructureRepository structureRepository;
    private PromotionLevel copper;
    private Build build;

    @Before
    public void before() {
        SecurityService securityService = mock(SecurityService.class);
        ValidationRunStatusService validationRunStatusService = mock(ValidationRunStatusService.class);
        structureRepository = mock(StructureRepository.class);
        service = new StructureServiceImpl(
                securityService,
                validationRunStatusService,
                structureRepository
        );
        // Model
        Project project = Project.of(nd("P", "Project")).withId(ID.of(1));
        Branch branch = Branch.of(project, nd("B", "Branch")).withId(ID.of(1));
        copper = PromotionLevel.of(branch, nd("COPPER", "")).withId(ID.of(1));
        build = Build.of(branch, nd("1", "Build 1"), Signature.of("test")).withId(ID.of(1));
    }

    @Test
    public void newPromotionRun_with_date() throws Exception {
        service.newPromotionRun(
                PromotionRun.of(
                        build,
                        copper,
                        Signature.of("test").withTime(LocalDateTime.of(2014, 9, 13, 18, 24)),
                        ""
                )
        );
        verify(structureRepository, times(1)).newPromotionRun(
                PromotionRun.of(
                        build,
                        copper,
                        Signature.of("test").withTime(LocalDateTime.of(2014, 9, 13, 18, 24)),
                        ""
                )
        );
    }

    @Test
    public void newPromotionRun_with_no_date() throws Exception {
        AtomicReference<PromotionRun> ref = new AtomicReference<>();
        when(structureRepository.newPromotionRun(any(PromotionRun.class))).then(
                invocation -> {
                    PromotionRun run = (PromotionRun) invocation.getArguments()[0];
                    ref.set(run);
                    return run;
                }
        );
        service.newPromotionRun(
                PromotionRun.of(
                        build,
                        copper,
                        Signature.of("test").withTime(null),
                        ""
                )
        );
        // Checks the signature's time
        assertNotNull(ref.get());
        assertNotNull(ref.get().getSignature().getTime());
    }
}