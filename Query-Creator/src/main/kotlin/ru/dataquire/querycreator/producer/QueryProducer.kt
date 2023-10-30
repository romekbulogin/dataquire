package ru.dataquire.querycreator.producer

import mu.KotlinLogging
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.core.MessagePropertiesBuilder
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import ru.dataquire.querycreator.request.QueryRequest
import ru.dataquire.querycreator.service.JwtService


@Service
class QueryProducer(
    private val rabbitTemplate: RabbitTemplate,
    private val bindingRequest: Binding,
    private val jwtService: JwtService
) {
    private val logger = KotlinLogging.logger { }
    fun sendQuery(request: QueryRequest, token: String): ResponseEntity<Any> {
        return try {
            val messageProperties =
                MessagePropertiesBuilder.newInstance().setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN).build()
            with(messageProperties) {
                setHeader("database", request.database)
                setHeader("dbms", request.dbms)
                setHeader(
                    "login", jwtService.extractUsername(
                        token.substring(7)
                    )
                )
            }
            val response = rabbitTemplate.convertSendAndReceive(
                bindingRequest.exchange,
                bindingRequest.routingKey,
                Message(request.sql.toByteArray(), messageProperties)
            )
            logger.info("Response: $response")
            ResponseEntity.ok().body(response)
        } catch (ex: Exception) {
            logger.error(ex.message)
            ResponseEntity.badRequest().body(
                mapOf(
                    "error" to ex.message
                )
            )
        }
    }
}