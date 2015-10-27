package net.nemerosa.ontrack.extension.svn.support;

import com.fasterxml.jackson.databind.node.ObjectNode;
import net.nemerosa.ontrack.extension.scm.support.TagPattern;
import net.nemerosa.ontrack.extension.svn.service.SVNService;
import net.nemerosa.ontrack.model.structure.StructureService;
import net.nemerosa.ontrack.model.support.NoConfig;
import org.junit.Before;
import org.junit.Test;

import static net.nemerosa.ontrack.json.JsonUtils.object;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class BuildSvnRevisionLinkMigrationActionTest {

    private SVNService svnService = mock(SVNService.class);
    private StructureService structureService = mock(StructureService.class);
    private RevisionSvnRevisionLink revisionLink = new RevisionSvnRevisionLink(structureService);
    private TagNamePatternSvnRevisionLink tagPatternLink = new TagNamePatternSvnRevisionLink(svnService, structureService);
    private TagNameSvnRevisionLink tagLink = new TagNameSvnRevisionLink(svnService, structureService);
    private BuildSvnRevisionLinkMigrationAction action;

    @Before
    public void before() {
        action = new BuildSvnRevisionLinkMigrationAction(revisionLink, tagPatternLink, tagLink);
    }

    @Test
    public void nodeMigration() {
        // Old node
        ObjectNode node = object()
                .with("branchPath", "/project/branches/1.1")
                .with("buildPath", "/project/tags/{build}")
                .end();
        // Migration
        action.migrateSvnBranchConfiguration(node);
        // Checks the node
        assertEquals(
                object()
                        .with("branchPath", "/project/branches/1.1")
                        .with("buildRevisionLink", object()
                                        .with("id", "tag")
                                        .with("data", object().end())
                                        .end()
                        )
                        .end(),
                node
        );
    }

    @Test
    public void tagName() {
        ConfiguredBuildSvnRevisionLink<?> c = action.toBuildSvnRevisionLinkConfiguration(
                "/project/tags/{build}"
        );
        assertTrue(c.getLink() instanceof TagNameSvnRevisionLink);
        assertTrue(c.getData() instanceof NoConfig);
    }

    @Test
    public void tagPatternName() {
        ConfiguredBuildSvnRevisionLink<?> c = action.toBuildSvnRevisionLinkConfiguration(
                "/project/tags/{build:1.1.*}"
        );
        assertTrue(c.getLink() instanceof TagNamePatternSvnRevisionLink);
        assertTrue(c.getData() instanceof TagPattern);
        TagPattern tagPattern = (TagPattern) c.getData();
        assertEquals("1.1.*", tagPattern.getPattern());
    }

    @Test
    public void tagPatternNameForTemplate() {
        ConfiguredBuildSvnRevisionLink<?> c = action.toBuildSvnRevisionLinkConfiguration(
                "/project/tags/{build:${sourceName}*}"
        );
        assertTrue(c.getLink() instanceof TagNamePatternSvnRevisionLink);
        assertTrue(c.getData() instanceof TagPattern);
        TagPattern tagPattern = (TagPattern) c.getData();
        assertEquals("${sourceName}*", tagPattern.getPattern());
    }

    @Test
    public void revision() {
        ConfiguredBuildSvnRevisionLink<?> c = action.toBuildSvnRevisionLinkConfiguration(
                "/project/branches/1.1@{build}"
        );
        assertTrue(c.getLink() instanceof RevisionSvnRevisionLink);
        assertTrue(c.getData() instanceof NoConfig);
    }

}
