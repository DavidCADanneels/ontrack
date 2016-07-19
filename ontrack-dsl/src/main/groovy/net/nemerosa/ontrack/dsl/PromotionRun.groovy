package net.nemerosa.ontrack.dsl

import net.nemerosa.ontrack.dsl.doc.DSL
import net.nemerosa.ontrack.dsl.properties.PromotionRunProperties

@DSL
class PromotionRun extends AbstractProjectResource {

    PromotionRun(Ontrack ontrack, Object node) {
        super(ontrack, node)
    }

    def call(Closure closure) {
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = this
        closure()
    }

    @Override
    // FIXME @DSL("Access to the properties of this promotion run.")
    PromotionRunProperties getConfig() {
        new PromotionRunProperties(ontrack, this)
    }

    @DSL("Gets the associated promotion level")
    def getPromotionLevel() {
        node.promotionLevel
    }
}
