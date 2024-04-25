package ru.dataquire.databasemanager.controller

import org.springframework.web.bind.annotation.*
import ru.dataquire.databasemanager.service.DatabaseService

@RestController
@RequestMapping("/api/database")
class DatabaseController(
    private val databaseService: DatabaseService,
) {
}