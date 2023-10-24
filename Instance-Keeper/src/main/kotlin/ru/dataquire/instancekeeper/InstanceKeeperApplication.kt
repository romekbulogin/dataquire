package ru.dataquire.instancekeeper

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
class InstanceKeeperApplication

fun main(args: Array<String>) {
    runApplication<InstanceKeeperApplication>(*args)
}
