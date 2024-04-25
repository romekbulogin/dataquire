package ru.dataquire.databasemanager.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.dataquire.databasemanager.entity.DatabaseEntity
import java.util.*

@Repository
interface DatabaseRepository : JpaRepository<DatabaseEntity, UUID> {
    fun findAllByDbms(dbms: String): List<DatabaseEntity>
    fun findByTitle(title: String): DatabaseEntity?
    fun findBySchema(schema: String): DatabaseEntity?
    fun findAllBySchema(schema: String): List<DatabaseEntity>
}