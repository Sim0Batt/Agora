package database.tables

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object TodosTable: IntIdTable("todos") {
    val title = varchar("title", 255)
    val description = text("description")
    val expirationDate = varchar("expirationDate", 255)
}