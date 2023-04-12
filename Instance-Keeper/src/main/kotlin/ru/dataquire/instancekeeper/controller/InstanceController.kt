package ru.dataquire.instancekeeper.controller

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.dataquire.instancekeeper.dto.FindInstance
import ru.dataquire.instancekeeper.entity.InstanceEntity
import ru.dataquire.instancekeeper.service.InstanceService

@RestController
@RequestMapping("/api/instances")
class InstanceController(private val instanceService: InstanceService) {
    @PostMapping("/find_by_dbms")
    fun findInstanceByDbms(@RequestBody request: FindInstance) = instanceService.findInstanceByDbms(request)

    @PostMapping("/save_instance")
    fun saveInstance(@RequestBody instanceEntity: InstanceEntity) = instanceService.saveInstance(instanceEntity)
}