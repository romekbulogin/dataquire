package ru.dataquire.databasemanager.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.dataquire.databasemanager.dto.Database
import ru.dataquire.databasemanager.dto.DatabaseForList
import ru.dataquire.databasemanager.entity.DatabaseEntity
import ru.dataquire.databasemanager.entity.UserEntity
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

    fun findDatabaseEntityByDatabaseNameAndDbmsAndSystemName(
        database: String,
        dbms: String,
        systemName: String
    ): DatabaseEntity

    fun findDatabaseEntityByUserEntityAndDbmsAndSystemNameAndDatabaseName(
        userEntity: UserEntity,
        dbms: String,
        systemName: String,
        databaseName: String
    ): DatabaseEntity

    fun findAllByUserEntity(userEntity: UserEntity): List<DatabaseForList>
    fun findDatabaseEntityByUserEntityAndSystemName(userEntity: UserEntity, systemName: String): Database
}