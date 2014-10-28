package net.nemerosa.ontrack.acceptance.boot

import net.nemerosa.ontrack.acceptance.ACCSearch
import net.nemerosa.ontrack.acceptance.ACCStructure
import org.junit.runner.JUnitCore
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class JUnitAcceptanceRunner implements AcceptanceRunner {

    private final Logger logger = LoggerFactory.getLogger(JUnitAcceptanceRunner)
    private final AcceptanceConfig config

    @Autowired
    JUnitAcceptanceRunner(AcceptanceConfig config) {
        this.config = config
    }

    @Override
    boolean run() throws Exception {
        logger.info "Starting acceptance tests."
        logger.info "Config: ${config}"

        // Config as system properties
        config.setSystemProperties()

        JUnitCore junit = new JUnitCore()
        // XML reporting
        XMLRunListener xmlRunListener = new XMLRunListener(System.out)
        junit.addListener(xmlRunListener)
        // FIXME Detection of classes
        // TODO Filtering on tests
        def result = junit.run(
//                ACCBrowserBasic,
                ACCSearch,
                ACCStructure
        )
        // XML output
        xmlRunListener.render(new File('ontrack-acceptance.xml'))
        // Result
        result.wasSuccessful()

    }
}
