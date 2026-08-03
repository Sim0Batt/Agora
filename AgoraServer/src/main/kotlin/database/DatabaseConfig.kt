package database

import org.jetbrains.exposed.sql.Database

object DatabaseConfig {
    fun getConnection(): Database {
        return Database.connect(
            url = "jdbc:mysql://localhost:3306/AgoraDB",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "YOUR_MYSQL_USER",
            password = "YOUR_MYSQL_PASSWORD",
        )
    }
}