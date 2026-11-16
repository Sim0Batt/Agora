package script

import database.tables.SongsTable
import io.ktor.util.debug.useContextElementInDebugMode
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import server.models.music.SongJson
import java.util.UUID

class SongsManager(val connection: Database) {
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
                        it[SongsTable.duration]
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
}
