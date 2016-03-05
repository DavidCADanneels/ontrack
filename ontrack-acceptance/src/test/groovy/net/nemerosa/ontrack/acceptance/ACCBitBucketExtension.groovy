package net.nemerosa.ontrack.acceptance

import net.nemerosa.ontrack.acceptance.support.AcceptanceTestSuite
import net.nemerosa.ontrack.dsl.http.OTNotFoundException
import net.nemerosa.ontrack.test.TestUtils
import org.junit.Test

/**
 * GUI tests about the `stash` extension (BitBucket).
 */
@AcceptanceTestSuite
class ACCBitBucketExtension extends AbstractACCDSL {

    /**
     * Regression test for #395
     */
    @Test
    void 'Creation and deletion of a configuration'() {
        String configurationName = TestUtils.uid('C') + '.org'
        // Creating the configuration
        ontrack.config.stash configurationName, url: 'https://bitbucket.org'
        // Getting the configuration by name
        def conf = ontrack.get("extension/stash/configurations/${configurationName}")
        assert conf.name == configurationName
        // Deletion
        ontrack.delete("extension/stash/configurations/${configurationName}")
        // Checks it's deleted
        try {
            ontrack.get("extension/stash/configurations/${configurationName}")
            assert false: 'Configuration should have been deleted'
        } catch (OTNotFoundException ignore) {
        }
    }

}
