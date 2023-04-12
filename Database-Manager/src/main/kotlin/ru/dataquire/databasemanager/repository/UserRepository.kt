package ru.dataquire.databasemanager.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.dataquire.databasemanager.entity.UserEntity
import java.util.Optional
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): UserEntity
    fun findByUsername(username: String): UserEntity

    fun findByActivatedUUID(uuid: String): UserEntity
}