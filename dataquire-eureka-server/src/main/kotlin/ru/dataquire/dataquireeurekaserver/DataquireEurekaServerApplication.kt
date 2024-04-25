package ru.dataquire.dataquireeurekaserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer

@SpringBootApplication
@EnableEurekaServer
class DataquireEurekaServerApplication

fun main(args: Array<String>) {
    runApplication<DataquireEurekaServerApplication>(*args)
}
