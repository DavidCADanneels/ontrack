package net.nemerosa.ontrack.extension.stash.property;

import net.nemerosa.ontrack.extension.stash.model.StashConfiguration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StashGitConfigurationTest {

    @Test
    public void bitbucketServer() {
        StashGitConfiguration gitConfiguration = new StashGitConfiguration(
                new StashProjectConfigurationProperty(
                        new StashConfiguration(
                                "server",
                                "http://stash.mycompany.com",
                                "", "", 0, ""
                        ), "nemerosa", "ontrack"
                )
        );
        assertFalse(gitConfiguration.isCloud());
        assertEquals("http://stash.mycompany.com/projects/nemerosa/repos/ontrack/commits/{commit}", gitConfiguration.getCommitLink());
        assertEquals("http://stash.mycompany.com/projects/nemerosa/repos/ontrack/browse/{path}?at={commit}", gitConfiguration.getFileAtCommitLink());
        assertEquals("http://stash.mycompany.com/scm/nemerosa/ontrack.git", gitConfiguration.getRemote());
    }

    @Test
    public void bitbucketCloud() {
        StashGitConfiguration gitConfiguration = new StashGitConfiguration(
                new StashProjectConfigurationProperty(
                        new StashConfiguration(
                                "cloud",
                                "https://bitbucket.org",
                                "", "", 0, ""
                        ), "nemerosa", "ontrack"
                )
        );
        assertTrue(gitConfiguration.isCloud());
        assertEquals("https://bitbucket.org/nemerosa/ontrack/commits/{commit}", gitConfiguration.getCommitLink());
        assertEquals("https://bitbucket.org/nemerosa/ontrack/src/{commit}/{path}", gitConfiguration.getFileAtCommitLink());
        assertEquals("https://bitbucket.org/nemerosa/ontrack.git", gitConfiguration.getRemote());
    }
}
