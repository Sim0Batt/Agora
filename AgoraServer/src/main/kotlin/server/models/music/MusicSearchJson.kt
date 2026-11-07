package server.models.music

import kotlinx.serialization.Serializable

@Serializable
class MusicSearchJson(
    val title: String
) {
    override fun toString(): String {
        return """
{
    "title": "$title"
}
        """.trimIndent()
    }
}