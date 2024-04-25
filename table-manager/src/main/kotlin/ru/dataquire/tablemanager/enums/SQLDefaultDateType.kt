package ru.dataquire.tablemanager.enums

import org.jooq.Field
import org.jooq.impl.DSL
import java.lang.Exception
import java.sql.Date

class SQLDefaultDateType {
    companion object {
        fun getSqlDefaultDateTypes() = listOf(
            "currentDate", "currentTime", "currentTimestamp",
            "now", "currentLocalDate", "currentLocalTime",
            "currentLocalDateTime", "currentOffsetTime",
            "currentOffsetDateTime", "currentInstant"
        )

        fun getSqlDefaultDateType(defaultValue: String): Any {
            return when (defaultValue) {
                "currentDate" -> DSL.currentDate()
                "currentTime" -> DSL.currentTime()
                "currentTimestamp" -> DSL.currentTimestamp()
                "now" -> DSL.now()
                "currentLocalDate" -> DSL.currentLocalDate()
                "currentLocalTime" -> DSL.currentLocalTime()
                "currentLocalDateTime" -> DSL.currentLocalDateTime()
                "currentOffsetTime" -> DSL.currentOffsetTime()
                "currentOffsetDateTime" -> DSL.currentOffsetDateTime()
                "currentInstant" -> DSL.currentInstant()
                else -> Exception("$defaultValue is not exist default value")
            }
        }
    }
}