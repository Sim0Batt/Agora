package database.dao

import database.tables.SongsTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

class SongsEntity(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, SongsEntity>(SongsTable)

    var title by SongsTable.title
    var author by SongsTable.author
    var duration by SongsTable.duration
    var path by SongsTable.path
}