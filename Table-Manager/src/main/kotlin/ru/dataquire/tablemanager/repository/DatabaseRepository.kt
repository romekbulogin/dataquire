package ru.dataquire.tablemanager.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.dataquire.tablemanager.dto.DatabaseForList
import ru.dataquire.tablemanager.entity.DatabaseEntity
import ru.dataquire.tablemanager.entity.UserEntity
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
    fun findDatabaseEntityByUserEntityAndSystemName(userEntity: UserEntity, systemName: String): DatabaseEntity
}