package database.tables

import org.jetbrains.exposed.v1.core.Table.PrimaryKey
import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable

object PlaylistSongTable : CompositeIdTable("playlist_songs") {
    val songId = varchar("song_id", 36).entityId()
    val playlistId = varchar("playlist_id", 36).entityId()

    override val primaryKey: PrimaryKey = PrimaryKey(songId, playlistId)
}