package ru.dataquire.querycreator.producer

import com.rabbitmq.client.BuiltinExchangeType
import com.rabbitmq.client.Channel
import mu.KotlinLogging
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.Connection
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import ru.dataquire.querycreator.request.QueryRequest
import ru.dataquire.querycreator.service.JwtService
import java.io.IOException


@Service
class QueryProducer(
    private val connection: Connection,
    private val rabbitTemplate: RabbitTemplate,
    private val jwtService: JwtService,
    private val exchange: DirectExchange
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
            val login = jwtService.extractUsername(
                token.substring(7)
            )
            val queue = "qc-queue-$login"
            val routingKey = "qc-rk-$login"
            var response: Any? = null

            val channel = connection.createChannel(true).use { channel ->
                val queueIsExist = checkQueueIsExist(channel, queue)
                if (queueIsExist) {
                    response = rabbitTemplate.convertSendAndReceive(
                        routingKey,
                        exchange.name,
                        Message(
                            request.sql.toByteArray(), messageProperties
                        )
                    )

                } else {
                    createQueueForUser(channel, queue, routingKey)
                    response = rabbitTemplate.convertSendAndReceive(
                        routingKey,
                        exchange.name,
                        Message(
                            request.sql.toByteArray(), messageProperties
                        )
                    )
                }
            }

            logger.info("[RESPONSE]: $response")
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

    private fun checkQueueIsExist(channel: Channel, queue: String) = try {
        channel.queueDeclarePassive(queue)
        true
    } catch (ex: IOException) {
        logger.error("Queue $queue is not exist. Start creating new queue...")
        false
    }

    private fun createQueueForUser(
        channel: Channel, queue: String, routingKey: String
    ) = try {
        channel.queueDeclare(
            queue, false, false, false, null
        )
        channel.queueBind(queue, exchange.name, routingKey, null)
//        BindingBuilder
//            .bind(Queue(queue, false))
//            .to(exchange)
//            .with(routingKey)
    } catch (ex: Exception) {
        logger.error("Error: ${ex.message}")
        throw ex
    }
}