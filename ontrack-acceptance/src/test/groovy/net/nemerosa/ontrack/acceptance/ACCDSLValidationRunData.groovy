package net.nemerosa.ontrack.acceptance

import net.nemerosa.ontrack.acceptance.support.AcceptanceTestSuite
import org.junit.Test

import static net.nemerosa.ontrack.test.TestUtils.uid

@AcceptanceTestSuite
class ACCDSLValidationRunData extends AbstractACCDSL {

    @Test
    void 'Validation run with data'() {
        def projectName = uid("P")
        // Validation stamp data type
        ontrack.project(projectName).branch("master") {
            validationStamp("VS").setDataType(
                    "net.nemerosa.ontrack.extension.general.validation.CHMLValidationDataType",
                    [
                            warningLevel: "HIGH",
                            warningValue: 10,
                            failedLevel : "CRITICAL",
                            failedValue : 1
                    ]
            )
        }
        // Creates a build and validates it
        def build = ontrack.branch(projectName, "master").build("1")
        build.validate("VS", [
                CRITICAL: 1,
                HIGH    : 2,
                MEDIUM  : 4,
                LOW     : 8,
        ])
        // Gets the run
        def run = build.validationRuns[0]
        // Gets the data
        assert run.data != null
        assert run.data.id == "net.nemerosa.ontrack.extension.general.validation.CHMLValidationDataType"
        assert run.data.data == [
                levels: [
                        CRITICAL: 1,
                        HIGH    : 2,
                        MEDIUM  : 4,
                        LOW     : 8,
                ]
        ]

    }

}
