package net.nemerosa.ontrack.dsl

import net.nemerosa.ontrack.dsl.properties.BuildProperties
import net.nemerosa.ontrack.dsl.properties.ProjectEntityProperties

class Build extends AbstractProjectResource {

    Build(Ontrack ontrack, Object node) {
        super(ontrack, node)
    }

    def call(Closure closure) {
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = this
        closure()
    }

    String getProject() {
        node?.branch?.project?.name
    }

    String getBranch() {
        node?.branch?.name
    }

    Build promote(String promotion) {
        post(link('promote'), [
                promotionLevel: ontrack.promotionLevel(project, branch, promotion).id,
        ])
        this
    }

    Build validate(String validationStamp, String validationStampStatus) {
        post(link('validate'), [
                validationStamp      : ontrack.validationStamp(project, branch, validationStamp).id,
                validationRunStatusId: validationStampStatus
        ])
        this
    }

    BuildProperties getConfig() {
        new BuildProperties(ontrack, this)
    }
}
