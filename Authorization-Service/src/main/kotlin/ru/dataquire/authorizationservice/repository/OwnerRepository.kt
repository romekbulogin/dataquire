package ru.dataquire.authorizationservice.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.dataquire.authorizationservice.entity.OwnerEntity
import java.util.Optional
import java.util.UUID

interface OwnerRepository : JpaRepository<OwnerEntity, UUID> {
    fun findByEmail(email: String): Optional<OwnerEntity>
    fun findByUsername(username: String): OwnerEntity
    fun findByActivatedUUID(uuid: String): OwnerEntity
}