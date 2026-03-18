package database

import org.jetbrains.exposed.sql.Database

object DatabaseConfig {
    fun getConnection(): Database {
        return Database.connect(
            url = "jdbc:postgresql://REDACTED-pooler.c-6.us-east-1.aws.neon.tech/neondb?user=neondb_owner&password=REDACTED&sslmode=require&channel_binding=require",
            driver = "org.postgresql.Driver",
            user = "neondb_owner",
            password = "REDACTED",
        )
    }
}