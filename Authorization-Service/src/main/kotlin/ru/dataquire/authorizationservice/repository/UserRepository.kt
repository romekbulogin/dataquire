package ru.dataquire.authorizationservice.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.dataquire.authorizationservice.entity.UserEntity
import java.util.Optional
import java.util.UUID

interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): Optional<UserEntity>
    fun findByUsername(username: String): UserEntity

    fun findByActivatedUUID(uuid: String): UserEntity
}