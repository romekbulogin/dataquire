package ru.dataquire.instancekeeper

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class InstanceKeeperApplication

fun main(args: Array<String>) {
    runApplication<InstanceKeeperApplication>(*args)
}
