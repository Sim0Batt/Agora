package database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable

object SongsTable: IdTable<String>("songs") {
    override val id: Column<EntityID<String>> = varchar("id", 36).entityId()
    override val primaryKey: PrimaryKey = PrimaryKey(id)

    var title = varchar("title", 100)
    var author = varchar("author", 100)
    var duration = integer("duration")
    var path = varchar("path", 255)
}