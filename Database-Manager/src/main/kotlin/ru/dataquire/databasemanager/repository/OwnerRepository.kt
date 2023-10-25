package ru.dataquire.databasemanager.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.dataquire.databasemanager.entity.OwnerEntity
import java.util.UUID

@Repository
interface OwnerRepository : JpaRepository<OwnerEntity, UUID> {
    fun findByEmail(email: String): OwnerEntity
    fun findByUsername(username: String): OwnerEntity

    fun findByActivatedUUID(uuid: String): OwnerEntity
}