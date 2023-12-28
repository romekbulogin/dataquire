package ru.dataquire.querycreator.configuration

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class RabbitConfiguration(
    private val connectionFactory: ConnectionFactory
) {
    @Value("\${spring.rabbitmq.consumer.exchange}")
    private val exchangeName: String? = null

    @Bean
    fun exchange() = DirectExchange(exchangeName)

    @Bean
    fun connection() = connectionFactory.createConnection()
}