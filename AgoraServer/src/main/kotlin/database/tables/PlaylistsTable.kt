package database.tables

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable

object PlaylistsTable: IdTable<String>("playlists") {
    override val id: Column<EntityID<String>> = varchar("id", 36).entityId()
    override val primaryKey: PrimaryKey = PrimaryKey(id)
    var title = varchar("title", 100)
    var count = integer("count")
}