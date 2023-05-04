package ru.dataquire.tablemanager.request

import ru.dataquire.tablemanager.request.key.ForeignKey
import ru.dataquire.tablemanager.request.key.PrimaryKey

data class Column(
    var name: String? = null,
    var dataType: String? = null,
    var length: Int = 0,
    var isNull: Boolean = false,
    var isUnique: Boolean = false,
    var isIdentity: Boolean = false,
    var isPrimaryKey: Boolean = false,
    var isForeignKey: Boolean = false,
    var defaultValue: Any? = null
)