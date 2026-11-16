package database.dao

import database.tables.PlaylistsTable
import database.tables.SongsTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

class PlaylistsEntity(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, PlaylistsEntity>(PlaylistsTable)

    var title by PlaylistsTable.title
    var count by PlaylistsTable.count
}