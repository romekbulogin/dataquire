package ru.dataquire.databasemanager.controller


import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.dataquire.databasemanager.request.ChangeCredentialsRequest
import ru.dataquire.databasemanager.request.DatabaseRequest
import ru.dataquire.databasemanager.request.DeleteDatabaseRequest
import ru.dataquire.databasemanager.service.DatabaseService

@RestController
@RequestMapping("/api/database")
class DatabaseController(
    private val databaseService: DatabaseService,
) {
    private val logger = KotlinLogging.logger { }

    @PostMapping("/create")
    fun createDatabase(
        @RequestBody request: DatabaseRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<Map<String, String?>> {
        logger.info("Request for create database: $request")
        return databaseService.createDatabase(request, token)
    }

    @PostMapping("/delete")
    fun deleteDatabase(
        @RequestBody request: DeleteDatabaseRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Request for delete database: $request")
        return databaseService.deleteDatabase(request, token)
    }

    @GetMapping("/my")
    fun viewDatabases(
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Request for view databases")
        return databaseService.findAllDatabases(token)
    }

    @GetMapping("/{systemName}")
    fun viewDatabase(
        @PathVariable(name = "systemName") systemName: String,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Request for view databases")
        return databaseService.findDatabase(token, systemName)
    }

    @PostMapping("/credentials/change")
    fun updateCredentials(
        @RequestBody request: ChangeCredentialsRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<Map<String, Any>> {
        logger.info("Request for update credentials")
        return databaseService.updateCredentials(request, token)
    }

    @GetMapping("/dbms")
    fun getDbmsList() = databaseService.getDbmsList()

    @GetMapping("/{dbms}/{systemName}/tables")
    fun getTablesOfDatabase(
        @RequestHeader("Authorization") token: String, @PathVariable systemName: String,
        @PathVariable dbms: String
    ) =
        databaseService.getTablesOfDatabase(token, systemName)
}