package net.nemerosa.ontrack.extension.api.support

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode
import net.nemerosa.ontrack.json.JsonUtils
import net.nemerosa.ontrack.model.form.Form
import net.nemerosa.ontrack.model.structure.AbstractValidationDataType
import net.nemerosa.ontrack.model.structure.ValidationRunStatusID
import org.springframework.stereotype.Component


/**
 * Configuration data - none
 */

/**
 * Data
 */
data class TestValidationData(
        val critical: Int,
        val high: Int,
        val medium: Int
)

/**
 * Validation type
 */
@Component
class TestValidationDataType(
        extensionFeature: TestExtensionFeature
) : AbstractValidationDataType<Unit, TestValidationData>(
        extensionFeature
) {

    override fun validateData(config: Unit?, data: TestValidationData) {
        validate(data.critical >= 0, "Number of critical issues must be >= 0")
        validate(data.high >= 0, "Number of high issues must be >= 0")
        validate(data.medium >= 0, "Number of medium issues must be >= 0")
    }

    override fun configToJson(config: Unit): JsonNode = NullNode.instance

    override fun configFromJson(node: JsonNode?): Unit? = null

    override fun getConfigForm(config: Unit?): Form = Form.create()

    override fun fromConfigForm(node: JsonNode) {}

    override fun toJson(data: TestValidationData): JsonNode = JsonUtils.format(data)

    override fun fromJson(node: JsonNode): TestValidationData? = JsonUtils.parse(node, TestValidationData::class.java)

    override fun getForm(data: TestValidationData?): Form =
            Form.create()
                    .with(
                            net.nemerosa.ontrack.model.form.Int
                                    .of("critical")
                                    .label("Critical issues")
                                    .min(0)
                                    .value(data?.critical)
                    )
                    .with(
                            net.nemerosa.ontrack.model.form.Int
                                    .of("high")
                                    .label("High issues")
                                    .min(0)
                                    .value(data?.high)
                    )
                    .with(
                            net.nemerosa.ontrack.model.form.Int
                                    .of("medium")
                                    .label("Medium issues")
                                    .min(0)
                                    .value(data?.medium)
                    )

    override fun fromForm(node: JsonNode): TestValidationData? =
            JsonUtils.parse(node, TestValidationData::class.java)

    override fun computeStatus(config: Unit?, data: TestValidationData): ValidationRunStatusID? =
            when {
                data.critical > 0 -> ValidationRunStatusID.STATUS_FAILED
                data.high > 0 -> ValidationRunStatusID.STATUS_WARNING
                else -> ValidationRunStatusID.STATUS_PASSED
            }

    override val displayName = "Test validation data"

}