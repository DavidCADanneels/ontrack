package net.nemerosa.ontrack.dsl

import net.nemerosa.ontrack.dsl.properties.BuildProperties

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

    PromotionRun promote(String promotion) {
        new PromotionRun(
                ontrack,
                ontrack.post(link('promote'), [
                        promotionLevel: ontrack.promotionLevel(project, branch, promotion).id,
                ])
        )
    }

    PromotionRun promote(String promotion, Closure closure) {
        def run = promote(promotion)
        run(closure)
        run
    }

    ValidationRun validate(String validationStamp, String validationStampStatus = 'PASSED') {
        new ValidationRun(
                ontrack,
                ontrack.post(link('validate'), [
                        validationStamp      : ontrack.validationStamp(project, branch, validationStamp).id,
                        validationRunStatusId: validationStampStatus
                ])
        )
    }

    ValidationRun validate(String validationStamp, String validationStampStatus = 'PASSED', Closure closure) {
        def run = validate(validationStamp, validationStampStatus)
        run(closure)
        run
    }

    List<PromotionRun> getPromotionRuns() {
        ontrack.get(link('promotionRuns')).resources.collect {
            new PromotionRun(ontrack, it)
        }
    }

    List<ValidationRun> getValidationRuns() {
        ontrack.get(link('validationRuns')).resources.collect {
            new ValidationRun(ontrack, it)
        }
    }

    BuildProperties getConfig() {
        new BuildProperties(ontrack, this)
    }
}
