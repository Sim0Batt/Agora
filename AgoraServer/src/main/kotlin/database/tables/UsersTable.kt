package database.tables

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object UsersTable: IntIdTable("Users"){
    val name = varchar("name", 20)
    val password = varchar("password", 50)
}