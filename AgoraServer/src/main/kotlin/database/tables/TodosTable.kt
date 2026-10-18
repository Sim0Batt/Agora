package database.tables

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object TodosTable: IntIdTable("Todos") {
    val title = varchar("title", 255)
    val description = text("description")
    val expirationDate = varchar("expirationDate", 255)
    val type = varchar("type", 100)
}