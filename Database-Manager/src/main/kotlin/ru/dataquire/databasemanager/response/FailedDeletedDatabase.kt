package ru.dataquire.databasemanager.response

import org.springframework.util.MultiValueMap

data class FailedDeletedDatabase(
    var error: String? = null,
    var exception: String? = null
)