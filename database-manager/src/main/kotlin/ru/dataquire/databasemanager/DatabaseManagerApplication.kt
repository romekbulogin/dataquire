package ru.dataquire.databasemanager

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
class DatabaseManagerApplication

fun main(args: Array<String>) {
    runApplication<DatabaseManagerApplication>(*args)
}
