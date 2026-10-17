package database

import org.jetbrains.exposed.sql.Database

object DatabaseConfig {
    fun getConnection(): Database {
        return Database.connect(
            url = "jdbc:mariadb://localhost:3306/AgoraDB",
            driver = "org.mariadb.jdbc.Driver",
            user = "root",
            password = "REDACTED",
        )
    }
}