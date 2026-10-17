package database.tables

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object LatexNotesTable: IntIdTable("LatexNotes"){
    val title = varchar("title", 100)
    val userId = integer("userId")
    val content = text("content")
    val type = varchar("type", 20)
}