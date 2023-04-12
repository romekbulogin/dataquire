package ru.dataquire.instancekeeper.service

import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import ru.dataquire.instancekeeper.dto.FindInstance
import ru.dataquire.instancekeeper.entity.InstanceEntity
import ru.dataquire.instancekeeper.repository.InstanceRepository

@Service
class InstanceService(private val instanceRepository: InstanceRepository) {
    private val logger = KotlinLogging.logger { }
    fun findInstanceByDbms(request: FindInstance): ResponseEntity<Any> {
        return try {
            logger.info(request.toString())
            ResponseEntity(instanceRepository.findByDbms(request.dbms!!), HttpStatus.OK)
        } catch (ex: Exception) {
            logger.error(ex.message)
            ResponseEntity(mapOf("error" to ex.message), HttpStatus.BAD_REQUEST)
        }
    }

    fun saveInstance(instanceEntity: InstanceEntity): ResponseEntity<Any> {
        return try {
            logger.info("[NEW INSTANCE]: $instanceEntity")
            ResponseEntity(instanceRepository.save(instanceEntity), HttpStatus.OK)
        } catch (ex: Exception) {
            logger.error(ex.message)
            ResponseEntity(mapOf("error" to ex.message), HttpStatus.BAD_REQUEST)
        }
    }
}