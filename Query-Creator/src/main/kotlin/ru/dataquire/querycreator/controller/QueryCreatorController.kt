package ru.dataquire.querycreator.controller

import org.springframework.web.bind.annotation.*
import ru.dataquire.querycreator.producer.QueryProducer
import ru.dataquire.querycreator.request.QueryRequest

@RestController
@RequestMapping("/api/query")
class QueryCreatorController(
    private val queryProducer: QueryProducer,
) {
    @PostMapping("/execute")
    fun sendQuery(
        @RequestBody request: QueryRequest,
        @RequestHeader(value = "Authorization") token: String
    ) = queryProducer.sendQuery(request, token)
}