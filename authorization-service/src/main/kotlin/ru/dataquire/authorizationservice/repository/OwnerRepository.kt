package ru.dataquire.authorizationservice.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.dataquire.authorizationservice.entity.OwnerEntity
import java.util.UUID

interface OwnerRepository : JpaRepository<OwnerEntity, UUID> {
    fun findByUsername(email: String): OwnerEntity
}