package ru.dataquire.databasemanager.feign

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import ru.dataquire.databasemanager.request.InstanceEntity

@FeignClient(value = "instance-keeper", url = "\${feign.instance-keeper.address}")
interface InstanceKeeperClient {
    @PostMapping("/api/instances/find/{dbms}")
    fun findInstanceByDbms(
        @PathVariable dbms: String
    ): InstanceEntity
}