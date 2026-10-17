package database.tables

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object NotesTable: IntIdTable("Notes"){
    val title = varchar("title", 100)
    val type = varchar("type", 20)
    val length = integer("length")
    val userId = integer("userId")
    val content = text("content")
}