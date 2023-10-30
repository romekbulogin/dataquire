package ru.dataquire.databasemanager.extension

class QueryForUser {
    companion object {
        private const val createUserQuery = "CREATE USER usertag WITH PASSWORD 'passtag';"
        fun convertCreateUserQuery(dbms: String) = when (dbms) {
            "PostgreSQL" -> createUserQuery
            "MySQL", "Oracle" -> {
                createUserQuery.replace("WITH PASSWORD", "IDENTIFIED BY")
                    .replace("'", "\"")
            }

            "MSSQL" -> {
                createUserQuery.replace("CREATE USER", "CREATE LOGIN")
                    .replace("WITH PASSWORD", "WITH PASSWORD =")
            }

            else -> {
                throw Exception("$dbms is not found")
            }
        }

        fun convertUpdateUsernameQuery(dbms: String) = when (dbms) {
            "PostgreSQL" -> "ALTER USER oldusername RENAME TO newusername;"
            "MySQL" -> "RENAME USER oldusername TO newusername;"
            "Oracle" -> "ALTER USER oldusername RENAME TO newusername;"
            "MSSQL" -> "ALTER LOGIN oldusername WITH NAME = newusername;"
            else -> {
                throw Exception("$dbms is not found")
            }
        }

        fun convertUpdatePasswordQuery(dbms: String) = when (dbms) {
            "PostgreSQL" -> "ALTER USER usertag WITH PASSWORD 'passtag';"
            "MySQL" -> "ALTER USER 'usertag'@'%' IDENTIFIED BY 'passtag';";
            "Oracle" -> "ALTER USER usertag IDENTIFIED BY passtag;"
            "MSSQL" -> "ALTER LOGIN usertag WITH PASSWORD = 'passtag';"
            else -> {
                throw Exception("$dbms is not found")
            }
        }


        fun convertDeleteUserQuery(dbms: String) = when (dbms) {
            "PostgreSQL" -> "DROP USER usertag;"
            "MySQL" -> "DROP USER 'usertag'@'%';";
            "Oracle" -> "DROP USER usertag;"
            "MSSQL" -> "DROP LOGIN usertag;"
            else -> {
                throw Exception("$dbms is not found")
            }
        }

        fun convertDatabaseGrant(
            dbms: String,
            systemName: String,
            username: String
        ) = when (dbms) {
            "PostgreSQL" -> "ALTER DATABASE $systemName OWNER TO $username"
            "MySQL" -> "GRANT ALL PRIVILEGES ON ${systemName}.* TO '${username}'@'%';";
            "Oracle" -> "DROP USER usertag;"
            "MSSQL" -> "ALTER AUTHORIZATION ON DATABASE::${systemName} TO $username"
            else -> {
                throw Exception("$dbms is not found")
            }
        }
    }
}