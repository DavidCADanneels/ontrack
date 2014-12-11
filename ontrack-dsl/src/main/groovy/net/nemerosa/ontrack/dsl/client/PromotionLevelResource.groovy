package net.nemerosa.ontrack.dsl.client

import com.fasterxml.jackson.databind.JsonNode
import net.nemerosa.ontrack.dsl.Ontrack
import net.nemerosa.ontrack.dsl.PromotionLevel
import net.nemerosa.ontrack.json.JsonUtils

class PromotionLevelResource extends AbstractProjectResource implements PromotionLevel {

    PromotionLevelResource(Ontrack ontrack, JsonNode node) {
        super(ontrack, node)
    }

    @Override
    String getProject() {
        JsonUtils.get(node.path('project'), 'name')
    }

    @Override
    String getBranch() {
        JsonUtils.get(node.path('project').path('branch'), 'name')
    }

}
