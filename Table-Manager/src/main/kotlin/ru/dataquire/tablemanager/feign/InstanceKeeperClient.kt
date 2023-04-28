package ru.dataquire.tablemanager.feign

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import ru.dataquire.tablemanager.feign.request.FindInstance
import ru.dataquire.tablemanager.feign.request.InstanceEntity

@FeignClient(value = "instance-keeper", url = "\${feign.instance-keeper.address}")
interface InstanceKeeperClient {
    @PostMapping("/api/instances/find_by_dbms")
    fun findInstanceByDbms(
        @RequestBody request: FindInstance
    ): InstanceEntity
}