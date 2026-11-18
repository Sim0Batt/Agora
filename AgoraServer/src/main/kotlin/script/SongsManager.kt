package script

import database.tables.SongsTable
import io.ktor.util.debug.useContextElementInDebugMode
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import server.models.music.SongJson
import java.io.File
import java.util.UUID
import java.util.logging.Logger

class SongsManager(val connection: Database, val logger: LoggerWriter) {
    fun saveSong(title: String, author: String, duration: Int, path: String): String {
        val songId = UUID.randomUUID().toString()
        try{
            transaction(connection) {
                SongsTable.insert {
                    it[SongsTable.title] = title
                    it[SongsTable.author] = author
                    it[SongsTable.duration] = duration
                    it[SongsTable.path] = path
                    it[SongsTable.id] = songId
                }
            }
            logger.info("Song $songId saved")
            return songId
        }catch (e: Exception){
            throw Exception("Error during song save ${e.message}: ${e.stackTraceToString()}")
        }
    }

    fun getSongs(): List<SongJson>{
        try {
            val songsList = transaction(connection){
                SongsTable.selectAll().map {
                    SongJson(
                        it[SongsTable.id].value,
                        it[SongsTable.title],
                        it[SongsTable.author],
                        it[SongsTable.duration],
                        ""
                    )
                }
            }
            return songsList
        }catch (e: Exception){
            throw Exception("Error retrieving songs ${e.message}: ${e.stackTraceToString()}")
        }
    }

    fun getSongPath(id: String): String{
        return transaction(connection){
            SongsTable.selectAll().where { SongsTable.id eq id }.first().let {
                it[SongsTable.path]
            }
        }
    }

    fun deleteSong(id: String){
        try {
            val songPath = transaction(connection){
                SongsTable.selectAll().where { SongsTable.id eq id }.first().let {
                    it[SongsTable.path]
                }
            }
            File(songPath).delete()
            transaction(connection){
                SongsTable.deleteWhere { SongsTable.id eq id }
            }
            logger.info("Song $id deleted")
        }catch (e: Exception){
            throw Exception("Error retrieving songs ${e.message}: ${e.stackTraceToString()}")
        }
    }
}
