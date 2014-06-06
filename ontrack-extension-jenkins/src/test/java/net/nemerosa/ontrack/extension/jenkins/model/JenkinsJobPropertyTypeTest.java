package net.nemerosa.ontrack.extension.jenkins.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import net.nemerosa.ontrack.extension.jenkins.JenkinsConfiguration;
import net.nemerosa.ontrack.extension.jenkins.JenkinsConfigurationService;
import net.nemerosa.ontrack.extension.jenkins.JenkinsJobProperty;
import net.nemerosa.ontrack.extension.jenkins.JenkinsJobPropertyType;
import net.nemerosa.ontrack.model.form.Field;
import net.nemerosa.ontrack.model.form.Form;
import net.nemerosa.ontrack.model.security.ProjectConfig;
import net.nemerosa.ontrack.model.security.SecurityService;
import net.nemerosa.ontrack.model.structure.*;
import org.junit.Before;
import org.junit.Test;

import static net.nemerosa.ontrack.json.JsonUtils.object;
import static net.nemerosa.ontrack.test.TestUtils.assertJsonEquals;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JenkinsJobPropertyTypeTest {

    private JenkinsJobPropertyType type;
    private SecurityService securityService;
    private JenkinsConfigurationService configurationService;

    @Before
    public void before() {
        securityService = mock(SecurityService.class);
        configurationService = mock(JenkinsConfigurationService.class);
        type = new JenkinsJobPropertyType(configurationService);
    }

    @Test
    public void applies() {
        assertTrue(type.applies(Project.class));
        assertTrue(type.applies(Branch.class));
        assertFalse(type.applies(PromotionLevel.class));
        assertTrue(type.applies(ValidationStamp.class));
        assertFalse(type.applies(Build.class));
        assertFalse(type.applies(ValidationRun.class));
    }

    @Test
    public void canEdit() {
        Project p1 = Project.of(new NameDescription("P1", "Project 1")).withId(ID.of(1));
        Project p2 = Project.of(new NameDescription("P2", "Project 2")).withId(ID.of(2));
        Branch b1 = Branch.of(p1, new NameDescription("B1", "Branch 1")).withId(ID.of(10));
        Branch b2 = Branch.of(p2, new NameDescription("B2", "Branch 2")).withId(ID.of(20));
        when(securityService.isProjectFunctionGranted(1, ProjectConfig.class)).thenReturn(true);
        when(securityService.isProjectFunctionGranted(2, ProjectConfig.class)).thenReturn(false);
        assertTrue(type.canEdit(b1, securityService));
        assertFalse(type.canEdit(b2, securityService));
    }

    @Test
    public void canView() {
        Project p1 = Project.of(new NameDescription("P1", "Project 1")).withId(ID.of(1));
        Project p2 = Project.of(new NameDescription("P2", "Project 2")).withId(ID.of(2));
        Branch b1 = Branch.of(p1, new NameDescription("B1", "Branch 1")).withId(ID.of(10));
        Branch b2 = Branch.of(p2, new NameDescription("B2", "Branch 2")).withId(ID.of(20));
        assertTrue(type.canView(b1, securityService));
        assertTrue(type.canView(b2, securityService));
    }

    @Test
    public void forStorage() throws JsonProcessingException {
        JenkinsConfiguration configuration = new JenkinsConfiguration(
                "MyConfig",
                "http://jenkins",
                "user",
                "secret"
        );
        assertJsonEquals(
                object()
                        .with("configuration", "MyConfig")
                        .with("job", "MyJob")
                        .end(),
                type.forStorage(
                        new JenkinsJobProperty(
                                configuration,
                                "MyJob"
                        )
                )
        );
    }

    @Test
    public void fromStorage() {
        JenkinsConfiguration configuration = new JenkinsConfiguration(
                "MyConfig",
                "http://jenkins",
                "user",
                "secret"
        );
        when(configurationService.getConfiguration("MyConfig")).thenReturn(configuration);
        // Stored JSON
        JsonNode node = object()
                .with("configuration", "MyConfig")
                .with("job", "MyJob")
                .end();
        // Reading
        assertEquals(
                new JenkinsJobProperty(
                        configuration,
                        "MyJob"
                ),
                type.fromStorage(node)
        );
    }

    @Test
    public void editionForm_for_new() {
        Form form = type.getEditionForm(null);
        assertEquals(2, form.getFields().size());
        {
            Field f = form.getField("configuration");
            assertNotNull(f);
            assertNull(f.getValue());
        }
        {
            Field f = form.getField("job");
            assertNotNull(f);
            assertNull(f.getValue());
        }
    }

    @Test
    public void editionForm_for_update() {
        JenkinsConfiguration configuration = new JenkinsConfiguration(
                "MyConfig",
                "http://jenkins",
                "user",
                "secret"
        );
        JenkinsJobProperty property = new JenkinsJobProperty(
                configuration,
                "MyJob"
        );
        Form form = type.getEditionForm(property);
        assertEquals(2, form.getFields().size());
        {
            Field f = form.getField("configuration");
            assertNotNull(f);
            assertEquals("MyConfig", f.getValue());
        }
        {
            Field f = form.getField("job");
            assertNotNull(f);
            assertEquals("MyJob", f.getValue());
        }
    }

}
