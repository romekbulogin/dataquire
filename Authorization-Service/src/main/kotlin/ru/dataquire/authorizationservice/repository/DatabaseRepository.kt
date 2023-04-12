package ru.dataquire.authorizationservice.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.dataquire.authorizationservice.entity.DatabaseEntity
import ru.dataquire.authorizationservice.entity.DatabaseEntityWithoutUser
import ru.dataquire.authorizationservice.entity.UserEntity
import java.util.UUID

@Repository
interface DatabaseRepository : JpaRepository<DatabaseEntity, UUID> {
    fun findDatabaseEntityByDatabaseNameAndAndDbmsAndAndUserEntity(
        database: String,
        dbms: String,
        userEntity: UserEntity
    ): DatabaseEntity

    fun findDatabaseEntityByDatabaseNameAndDbms(
        database: String,
        dbms: String,
    ): DatabaseEntity

    fun findAllByUserEntity(userEntity: UserEntity): List<DatabaseEntityWithoutUser>
}