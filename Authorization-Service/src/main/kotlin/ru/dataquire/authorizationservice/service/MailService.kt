package ru.dataquire.authorizationservice.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class MailService(private val mailSender: JavaMailSender) {
    private val logger = KotlinLogging.logger { }

    @Value("\${spring.mail.username}")
    private val email: String? = null
    fun sendMessageVerify(to: String, subject: String, text: String) = runCatching {
        val message = SimpleMailMessage().apply {
            from = email
            setTo(to)
            setSubject(subject)
            setText(text)
        }
        mailSender.send(message)
    }.onFailure {
        logger.error(it.message)
    }
}