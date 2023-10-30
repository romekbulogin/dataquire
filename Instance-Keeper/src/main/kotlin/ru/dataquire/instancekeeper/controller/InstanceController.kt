package ru.dataquire.instancekeeper.controller

import org.springframework.web.bind.annotation.*
import ru.dataquire.instancekeeper.entity.InstanceEntity
import ru.dataquire.instancekeeper.service.InstanceService

@RestController
@RequestMapping("/api/instances")
class InstanceController(
    private val instanceService: InstanceService
) {
    @GetMapping("/find/{dbms}")
    fun findInstanceByDbms(@PathVariable dbms: String) =
        instanceService.findInstanceByDbms(dbms)

    @PostMapping("/save")
    fun saveInstance(@RequestBody instanceEntity: InstanceEntity) =
        instanceService.saveInstance(instanceEntity)
}