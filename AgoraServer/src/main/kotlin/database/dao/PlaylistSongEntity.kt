package database.dao

import database.tables.PlaylistSongTable
import org.jetbrains.exposed.v1.core.dao.id.CompositeID
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.CompositeEntity
import org.jetbrains.exposed.v1.dao.CompositeEntityClass

class PlaylistSongEntity(id: EntityID<CompositeID>) : CompositeEntity(id) {
    companion object : CompositeEntityClass<PlaylistSongEntity>(PlaylistSongTable)

    var songId by PlaylistSongTable.songId
    var playlistId by PlaylistSongTable.playlistId
}