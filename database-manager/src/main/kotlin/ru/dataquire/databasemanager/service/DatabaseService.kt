package ru.dataquire.databasemanager.service

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import ru.dataquire.databasemanager.entity.DatabaseEntity
import ru.dataquire.databasemanager.feign.InstanceKeeperClient
import ru.dataquire.databasemanager.repository.DatabaseRepository


@Service
class DatabaseService(
    private val databaseRepository: DatabaseRepository,
    private val instanceKeeperClient: InstanceKeeperClient,
) {
    private val logger = LoggerFactory.getLogger(DatabaseService::class.java)

    fun findAllByDbms(dbms: String): ResponseEntity<*> = try {
        logger.info("[FIND ALL BY DBMS] dbms={$dbms}")
        ResponseEntity.ok().body(
            databaseRepository.findAllByDbms(dbms)
        )
    } catch (ex: Exception) {
        logger.error(ex.message)
        ResponseEntity
            .badRequest()
            .body(
                mapOf("error" to ex.message)
            )
    }
}