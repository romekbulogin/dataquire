package ru.dataquire.tablemanager.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.dataquire.tablemanager.entity.UserEntity
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): UserEntity
    fun findByUsername(username: String): UserEntity

    fun findByActivatedUUID(uuid: String): UserEntity
}