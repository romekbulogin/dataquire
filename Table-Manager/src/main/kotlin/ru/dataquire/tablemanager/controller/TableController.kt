package ru.dataquire.tablemanager.controller

import org.springframework.web.bind.annotation.*
import ru.dataquire.tablemanager.request.CreateTableRequest
import ru.dataquire.tablemanager.request.ViewTableRequest
import ru.dataquire.tablemanager.service.TableManagerService

@RestController
@RequestMapping("/api/tables")
class TableController(private val tableManagerService: TableManagerService) {

    @PostMapping("/view")
    fun viewTable(@RequestHeader("Authorization") token: String, @RequestBody request: ViewTableRequest) =
        tableManagerService.viewTable(token, request)

    @PostMapping("/create")
    fun createTable(@RequestHeader("Authorization") token: String, @RequestBody request: CreateTableRequest) =
        tableManagerService.createTable(token, request)

    @GetMapping("/data/types")
    fun getSqlDataTypes() = tableManagerService.getSqlDataTypes()

    @GetMapping("/date/types")
    fun getSqlDateTypes() = tableManagerService.getDefaultDateTypes()

    @PostMapping("/keys")
    fun test(@RequestHeader("Authorization") token: String, @RequestBody request: ViewTableRequest) =
        tableManagerService.getColumnForForeignKey(token, request)

    @GetMapping("/{dbms}/{systemName}/{table}/structure")
    fun getColumnsOfTable(
        @RequestHeader("Authorization") token: String, @PathVariable systemName: String,
        @PathVariable dbms: String, @PathVariable table: String
    ) =
        tableManagerService.getColumnsOfTable(token, systemName, table)
}