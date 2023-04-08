package ru.dataquire.querycreator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class QueryCreatorApplication

fun main(args: Array<String>) {
    runApplication<QueryCreatorApplication>(*args)
}
