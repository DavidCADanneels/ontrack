package net.nemerosa.ontrack.boot.ui

import net.nemerosa.ontrack.json.JsonUtils
import net.nemerosa.ontrack.model.security.ValidationStampCreate
import net.nemerosa.ontrack.model.structure.ServiceConfiguration
import net.nemerosa.ontrack.model.structure.ThresholdPercentageValidationDataType
import net.nemerosa.ontrack.model.structure.ThresholdPercentageValidationDataTypeConfig
import net.nemerosa.ontrack.model.structure.ValidationStampInput
import net.nemerosa.ontrack.test.TestUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class ValidationStampControllerIT : AbstractWebTestSupport() {

    @Autowired
    private lateinit var validationStampController: ValidationStampController

    @Test
    fun validationStampWithDataType() {
        // Branch
        val branch = doCreateBranch()
        // Creates a validation stamp with an associated percentage data type
        val vs = asUser().with(branch, ValidationStampCreate::class.java).call {
            validationStampController.newValidationStamp(
                    branch.id,
                    ValidationStampInput(
                            "VSPercent",
                            "",
                            ServiceConfiguration(
                                    ThresholdPercentageValidationDataType::class.java.name,
                                    JsonUtils.format(
                                            ThresholdPercentageValidationDataTypeConfig(null)
                                    )
                            )
                    )
            )
        }
        // Loads the validation stamp
        val loadedVs = validationStampController.getValidationStamp(vs.id)
        // Checks the data type is still there
        val dataType = loadedVs.dataType
        assertNotNull("Data type is loaded", dataType)
        assertEquals(ThresholdPercentageValidationDataType::class.java.name, dataType.id)
        TestUtils.assertJsonEquals(
                JsonUtils.`object`()
                        .withNull("threshold")
                        .with("okIfGreater", false)
                        .end(),
                dataType.data
        )
    }

}
