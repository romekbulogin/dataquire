package ru.dataquire.instancekeeper.service

import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import ru.dataquire.instancekeeper.entity.InstanceEntity
import ru.dataquire.instancekeeper.repository.InstanceRepository

@Service
class InstanceService(
    private val instanceRepository: InstanceRepository
) {
    private val logger = KotlinLogging.logger { }
    fun findInstanceByDbms(dbms: String) = try {
        logger.info("[FIND INSTANCE]: $dbms")
        val instance = instanceRepository.findByDbms(dbms)
        ResponseEntity.ok().body(instance)
    } catch (ex: Exception) {
        logger.error(ex.message)
        ResponseEntity(mapOf("error" to ex.message), HttpStatus.BAD_REQUEST)
    }

    fun saveInstance(instanceEntity: InstanceEntity) = try {
        logger.info("[NEW INSTANCE]: $instanceEntity")
        instanceRepository.save(instanceEntity)
        ResponseEntity.ok().body(
            mapOf("status" to "Successfully")
        )
    } catch (ex: Exception) {
        logger.error(ex.message)
        ResponseEntity.badRequest().body(
            mapOf("error" to ex.message)
        )
    }
}