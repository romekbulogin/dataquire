package ru.dataquire.querycreator.controller

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.dataquire.querycreator.rabbit.producer.QueryProducer
import ru.dataquire.querycreator.request.QueryRequest

@RestController
@RequestMapping("/api/query_executor")
class QueryCreatorController(
    private val queryProducer: QueryProducer,
) {

    @PostMapping("/send_query")
    fun sendQuery(
        @RequestBody request: QueryRequest,
        @RequestHeader(value = "Authorization") token: String
    ): ResponseEntity<Any> = queryProducer.sendQuery(request, token)
}