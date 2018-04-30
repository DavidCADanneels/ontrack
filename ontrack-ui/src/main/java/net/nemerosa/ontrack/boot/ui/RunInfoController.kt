package net.nemerosa.ontrack.boot.ui

import net.nemerosa.ontrack.model.structure.RunInfo
import net.nemerosa.ontrack.model.structure.RunInfoService
import net.nemerosa.ontrack.model.structure.RunnableEntityType
import net.nemerosa.ontrack.model.structure.StructureService
import net.nemerosa.ontrack.ui.controller.AbstractResourceController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/structure/run-info")
class RunInfoController(
        private val structureService: StructureService,
        private val runInfoService: RunInfoService
) : AbstractResourceController() {

    @GetMapping("{runnableEntityType}/{id}")
    fun getRunInfo(
            @PathVariable runnableEntityType: RunnableEntityType,
            @PathVariable id: Int
    ): RunInfo =
            runInfoService.getRunInfo(runnableEntityType, id)

}