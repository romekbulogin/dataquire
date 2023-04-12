package ru.dataquire.databasemanager.feign

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import ru.dataquire.databasemanager.feign.request.FindInstance
import ru.dataquire.databasemanager.feign.request.InstanceEntity

@FeignClient(value = "instance-keeper", url = "http://localhost:8084/")
interface InstancesManagerClient {
    @PostMapping("/api/instances/find_by_dbms")
    fun findInstanceByDbms(
        @RequestBody request: FindInstance
    ): InstanceEntity
}