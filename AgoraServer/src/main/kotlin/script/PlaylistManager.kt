package script

import database.tables.PlaylistSongTable
import database.tables.PlaylistsTable
import database.tables.SongsTable
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.plus
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import server.models.music.PlaylistJson
import server.models.music.SongJson
import java.util.UUID

class PlaylistManager(val connection: Database, val logger: LoggerWriter) {
    fun createPlaylist(title: String): String{
        try{
            logger.info("Creating playlist $title")
            return transaction(connection) {
                PlaylistsTable.insert {
                    it[PlaylistsTable.id] = UUID.randomUUID().toString()
                    it[PlaylistsTable.title] = title
                    it[PlaylistsTable.count] = 0
                }[PlaylistsTable.id].toString()
            }
        }catch (e: Exception){
            throw Exception("Error while creating the playlist ${e.message}: ${e.stackTraceToString()}")
        }
    }

    fun removePlaylist(id: String){
        try{
            transaction(connection) {
                PlaylistsTable.deleteWhere {
                    PlaylistsTable.id eq id
                }
            }
            logger.info("Removed playlist $id")
        }catch (e: Exception){
            throw Exception("Error while deleting the playlist ${e.message}: ${e.stackTraceToString()}")
        }
    }

    fun addSong(playlistId: String, songId: String){
        try{
            transaction(connection) {
                PlaylistSongTable.insert {
                    it[PlaylistSongTable.songId] = songId
                    it[PlaylistSongTable.playlistId] = playlistId
                }
                PlaylistsTable.update({ PlaylistsTable.id eq playlistId }) {
                    it[PlaylistsTable.count] = PlaylistsTable.count + 1
                }
            }
            logger.info("Added song $songId to playlist $playlistId")
        }catch (e: Exception){
            throw Exception("Error while inserting the song in the playlist ${e.message}: ${e.stackTraceToString()}")
        }
    }

    fun getPlaylists(): List<PlaylistJson>{
        try{
            return transaction(connection) {
                PlaylistsTable.selectAll().map {
                    PlaylistJson(
                        it[PlaylistsTable.id].value,
                        it[PlaylistsTable.title],
                        it[PlaylistsTable.count]
                    )
                }
            }
        }catch (e: Exception){
            throw Exception("Error while retrieving playlists ${e.message}: ${e.stackTraceToString()}")
        }
    }

    fun getSongsForPlaylist(playlistId: String): List<SongJson>{
        return transaction(connection) {
            PlaylistSongTable
                .join(
                    SongsTable,
                    JoinType.INNER,
                    onColumn = PlaylistSongTable.songId,
                    otherColumn = SongsTable.id
                )
                .selectAll()
                .where { PlaylistSongTable.playlistId eq playlistId }
                .map {
                    SongJson(
                        it[SongsTable.id].value,
                        it[SongsTable.title],
                        it[SongsTable.author],
                        it[SongsTable.duration],
                        ""
                    )
                }
        }
    }

    fun removeSongFromPlaylist(songId: String, playlistId: String){
        try{
            transaction(connection){
                PlaylistSongTable.deleteWhere {
                    (PlaylistSongTable.songId eq songId) and (PlaylistSongTable.playlistId eq playlistId)
                }
            }
            logger.info("Removed song $songId from playlist $playlistId")
        }catch (e: Exception){
            throw Exception("Error while removing song from playlist ${e.message}: ${e.stackTraceToString()}")
        }
    }
}