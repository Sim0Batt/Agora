package database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object NotesTable: IntIdTable("Notes"){
    val title = varchar("title", 100)
    val type = varchar("type", 20)
    val length = integer("length")
    val userId = integer("userId")
}