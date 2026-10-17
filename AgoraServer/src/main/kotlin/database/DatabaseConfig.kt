package database

import org.jetbrains.exposed.v1.jdbc.Database

object DatabaseConfig {
    fun getConnection(): Database {
        return Database.connect(
            url = "jdbc:mariadb://localhost:3306/AgoraDB",
            driver = "org.mariadb.jdbc.Driver",
            user = "root",
            password = "REDACTED"
        )
    }

    fun getGaiaConnection(): Database{
        return Database.connect(
            url = "jdbc:mariadb://localhost:3306/Gaia",
            driver = "org.mariadb.jdbc.Driver",
            user = "root",
            password = "REDACTED"
        )
    }
}