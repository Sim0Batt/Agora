package server.models.music

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import javax.management.monitor.StringMonitor
@OptIn(ExperimentalSerializationApi::class)
@JsonIgnoreUnknownKeys
@Serializable
class SongJson (
    val id: String,
    val title: String,
    val author: String,
    val duration: Int,
    val url: String
){
    override fun toString(): String {
        return """
{
    "id": "$id",
    "title": "$title",
    "author": "$author",
    "duration": $duration,
    "url": "$url"
}
        """.trimIndent()
    }

}