package ru.dataquire.databasemanager.controller

import org.springframework.web.bind.annotation.*
import ru.dataquire.databasemanager.request.ChangeCredentialsRequest
import ru.dataquire.databasemanager.request.DatabaseRequest
import ru.dataquire.databasemanager.request.DeleteDatabaseRequest
import ru.dataquire.databasemanager.request.OwnDatabaseRequest
import ru.dataquire.databasemanager.service.DatabaseService

@RestController
@RequestMapping("/api/database")
class DatabaseController(
    private val databaseService: DatabaseService,
) {
    @PostMapping("/create")
    fun createDatabase(
        @RequestBody request: DatabaseRequest,
        @RequestHeader("Authorization") token: String
    ) = databaseService.createDatabase(request, token)

    @DeleteMapping("/delete")
    fun deleteDatabase(
        @RequestBody request: DeleteDatabaseRequest,
        @RequestHeader("Authorization") token: String
    ) = databaseService.deleteDatabase(request, token)

    @GetMapping("/my")
    fun viewDatabases(
        @RequestHeader("Authorization") token: String
    ) = databaseService.findAllDatabases(token)

    @GetMapping("/{systemName}")
    fun viewDatabase(
        @PathVariable(name = "systemName") systemName: String,
        @RequestHeader("Authorization") token: String
    ) = databaseService.findDatabase(token, systemName)

    @GetMapping("/find/{dbms}")
    fun viewDatabaseInDBMS(
        @PathVariable(name = "dbms") dbms: String,
        @RequestHeader("Authorization") token: String
    ) = databaseService.findDatabaseInDBMS(token, dbms)

    @PostMapping("/credentials/change")
    fun updateCredentials(
        @RequestBody request: ChangeCredentialsRequest,
        @RequestHeader("Authorization") token: String
    ) = databaseService.updateCredentials(request, token)

    @PostMapping("/external/add")
    fun addYourOwnDatabase(
        @RequestBody request: OwnDatabaseRequest,
        @RequestHeader("Authorization") token: String
    ) = databaseService.addYourOwnDatabase(request, token)

    @DeleteMapping("/external/delete")
    fun deleteYourOwnDatabase(
        @RequestBody request: DeleteDatabaseRequest,
        @RequestHeader("Authorization") token: String
    ) = databaseService.deleteYourOwnDatabase(request, token)

    @GetMapping("/{systemName}/{table}/structure")
    fun getStructureOfTable(
        @RequestHeader("Authorization") token: String, @PathVariable systemName: String, @PathVariable table: String
    ) = databaseService.getDatabaseStructure(token, systemName, table)

    @GetMapping("/{dbms}/{systemName}/tables")
    fun getTablesOfDatabase(
        @RequestHeader("Authorization") token: String, @PathVariable systemName: String,
        @PathVariable dbms: String
    ) =
        databaseService.getTablesOfDatabase(token, systemName)
}