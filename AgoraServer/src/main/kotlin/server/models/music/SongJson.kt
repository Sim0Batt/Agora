package server.models.music

import kotlinx.serialization.Serializable
import javax.management.monitor.StringMonitor

@Serializable
class SongJson (
    val id: String,
    val title: String,
    val author: String,
    val duration: Int
){
    override fun toString(): String {
        return """
{
    "id": "$id",
    "title": "$title",
    "author": "$author",
    "duration": $duration
}
        """.trimIndent()
    }

}