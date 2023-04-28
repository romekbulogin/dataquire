package ru.dataquire.tablemanager.request

data class Column(
    var name: String? = null,
    var dataType: String? = null,
    var length: Int = 0,
    var isNull: Boolean? = null,
    var isIdentity: Boolean? = null
)