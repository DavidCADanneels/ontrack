package net.nemerosa.ontrack.dsl

import net.nemerosa.ontrack.dsl.doc.DSL
import net.nemerosa.ontrack.dsl.doc.DSLMethod
import net.nemerosa.ontrack.dsl.properties.ProjectEntityProperties
import net.nemerosa.ontrack.dsl.properties.ValidationStampProperties

@DSL
class ValidationStamp extends AbstractProjectResource {

    ValidationStamp(Ontrack ontrack, Object node) {
        super(ontrack, node)
    }

    @DSLMethod("Name of the associated project.")
    String getProject() {
        node?.branch?.project?.name
    }

    @DSLMethod("Name of the associated branch.")
    String getBranch() {
        node?.branch?.name
    }

    @DSLMethod("Configuration of the promotion level with a closure.")
    def call(Closure closure) {
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = this
        closure()
    }

    ProjectEntityProperties getConfig() {
        new ValidationStampProperties(ontrack, this)
    }

    @DSLMethod("Sets the validation stamp image (see <<dsl-usecases-images>>)")
    def image(Object o) {
        image(o, 'image/png')
    }

    @DSLMethod("Sets the validation stamp image (see <<dsl-usecases-images>>)")
    def image(Object o, String contentType) {
        ontrack.upload(link('image'), 'file', o, contentType)
    }

    @DSLMethod("Gets the validation stamp image (see <<dsl-usecases-images>>)")
    Document getImage() {
        ontrack.download(link('image'))
    }

    /**
     * Validation stamp weather decoration: <code>weather</code> and <code>text</code>
     */
    @DSLMethod("Gets the validation stamp weather decoration.")
    def getValidationStampWeatherDecoration() {
        getDecoration('net.nemerosa.ontrack.extension.general.ValidationStampWeatherDecorationExtension')
    }

    @DSLMethod("Sets a data type for the validation stamp")
    def setDataType(String id, Object config) {
        ontrack.put(
                link("update"),
                [
                        name       : name,
                        description: description,
                        dataType   : [
                                id  : id,
                                data: config,
                        ],
                ]
        )
    }

    @DSLMethod("Gets the data type for the validation stamp, map with `id` and `config`, or null if not defined.")
    def getDataType() {
        if (node.dataType) {
            return [
                    id    : node.dataType.descriptor.id,
                    config: node.dataType.config
            ]
        } else {
            return null
        }
    }

    @DSLMethod("Sets the data type for this validation stamp to 'text'.")
    def setTextDataType() {
        setDataType(
                "net.nemerosa.ontrack.extension.general.validation.TextValidationDataType",
                [:]
        )
    }
}
