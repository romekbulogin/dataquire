package ru.dataquire.tablemanager.controller

import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.dataquire.tablemanager.request.CreateTableRequest
import ru.dataquire.tablemanager.request.ViewTableRequest
import ru.dataquire.tablemanager.service.TableManagerService

@RestController
@RequestMapping("/api/tables")
class TableController(private val tableManagerService: TableManagerService) {
    private val logger = KotlinLogging.logger { }

    @PostMapping("/view")
    fun viewTable(@RequestHeader("Authorization") token: String, @RequestBody request: ViewTableRequest): Any {
        logger.info("Request for view table: $request")
        return tableManagerService.viewTable(token, request)
    }

    @PostMapping("/create")
    fun createTable(@RequestHeader("Authorization") token: String, @RequestBody request: CreateTableRequest) =
        tableManagerService.createTable(token, request)

    @GetMapping("/data/types")
    fun getSqlDataTypes() = tableManagerService.getSqlDataTypes()

    @GetMapping("/date/types")
    fun getSqlDateTypes() = tableManagerService.getDefaultDateTypes()

    @PostMapping("/keys")
    fun getKeysColumn(@RequestHeader("Authorization") token: String, @RequestBody request: ViewTableRequest) =
        tableManagerService.getColumnForForeignKey(token, request)

    @PostMapping("/drop/{systemName}/{tableName}")
    fun dropTable(
        @RequestHeader("Authorization") token: String,
        @PathVariable tableName: String,
        @PathVariable systemName: String
    ) =
        tableManagerService.dropTable(token, tableName, systemName)

    @PostMapping("/update_raw/{systemName}/{tableName}")
    fun updateRawInTable(
        @RequestHeader("Authorization") token: String,
        @PathVariable tableName: String,
        @PathVariable systemName: String,
        @RequestBody rows: List<Map<String, Any?>>
    ) = tableManagerService.updateRowInTable(token, tableName, systemName, rows)
}