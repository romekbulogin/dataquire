package ru.dataquire.databasemanager.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.dataquire.databasemanager.dto.Database
import ru.dataquire.databasemanager.dto.DatabaseForList
import ru.dataquire.databasemanager.dto.DatabaseInDBMS
import ru.dataquire.databasemanager.entity.DatabaseEntity
import ru.dataquire.databasemanager.entity.OwnerEntity
import java.util.UUID

@Repository
interface DatabaseRepository : JpaRepository<DatabaseEntity, UUID> {

    fun findByDatabaseNameAndDbmsAndOwnerEntity(
        database: String,
        dbms: String,
        ownerEntity: OwnerEntity
    ): DatabaseEntity

    fun findByDatabaseNameAndDbms(
        database: String,
        dbms: String,
    ): DatabaseEntity

    fun findByDatabaseNameAndDbmsAndSystemName(
        database: String,
        dbms: String,
        systemName: String
    ): DatabaseEntity

    fun findByOwnerEntityAndDbmsAndSystemNameAndDatabaseName(
        ownerEntity: OwnerEntity,
        dbms: String,
        systemName: String,
        databaseName: String
    ): DatabaseEntity

    fun findAllByOwnerEntity(ownerEntity: OwnerEntity): List<DatabaseForList>
    fun findByOwnerEntityAndSystemName(
        ownerEntity: OwnerEntity,
        systemName: String
    ): Database

    fun findAllByOwnerEntityAndDbms(
        ownerEntity: OwnerEntity,
        dbms: String
    ): List<DatabaseInDBMS>
}