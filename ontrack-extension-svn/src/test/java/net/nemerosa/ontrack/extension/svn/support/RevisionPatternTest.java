package net.nemerosa.ontrack.extension.svn.support;

import org.junit.Test;

import static org.junit.Assert.*;

public class RevisionPatternTest {

    // TODO Checks constructor

    @Test
    public void validity() {
        RevisionPattern r = new RevisionPattern("11.8.4.*-{revision}");
        assertFalse(r.isValidBuildName("11.8.5.0-123456"));
        assertFalse(r.isValidBuildName("11.8.4-123456"));
        assertTrue(r.isValidBuildName("11.8.4.5-123456"));
        assertFalse(r.isValidBuildName("11.8.4.5-v123456"));
    }

    @Test
    public void extract_revision() {
        RevisionPattern r = new RevisionPattern("11.8.4.*-{revision}");
        assertFalse(r.extractRevision("11.8.5.0-123456").isPresent());
        assertFalse(r.extractRevision("11.8.4-123456").isPresent());
        assertEquals(123456L, r.extractRevision("11.8.4.5-123456").getAsLong());
        assertFalse(r.extractRevision("11.8.4.5-v123456").isPresent());
    }

}
