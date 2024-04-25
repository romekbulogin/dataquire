package ru.dataquire.tablemanager.dto

data class Column(
    var field: String? = null,
    var type: String? = null,
    var editable: Boolean = true
)
