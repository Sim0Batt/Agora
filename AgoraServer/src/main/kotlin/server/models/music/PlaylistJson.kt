package server.models.music

import kotlinx.serialization.Serializable

@Serializable
class PlaylistJson(
    val id: String,
    val title: String,
    val count: Int
) {
    override fun toString(): String {
        return """
{
    "id": "$id",
    "title": "$title",
    "count": $count
}
        """.trimIndent()
    }
}