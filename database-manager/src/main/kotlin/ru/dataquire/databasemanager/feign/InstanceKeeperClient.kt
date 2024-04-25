package ru.dataquire.databasemanager.feign

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import ru.dataquire.databasemanager.dto.InstanceEntity

@FeignClient(value = "instance-keeper", url = "\${feign.instance-keeper.address}")
interface InstanceKeeperClient {
    @GetMapping("/api/instances/find/{dbms}")
    fun findInstanceByDbms(
        @PathVariable dbms: String
    ): InstanceEntity

    @GetMapping("/api/instances/find")
    fun findInstanceByTitle(
        @RequestParam title: String
    ): InstanceEntity
}