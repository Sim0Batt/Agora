package server.models.notes

import kotlinx.serialization.Serializable

@Serializable
class NotesJson(
    val id: Int,
    val title: String,
    val type: String,
    val length: Int,
    val content: String = ""
) {
    override fun toString(): String {
        return """
{
    "id":"$id",
    "title":"$title",
    "length":"$length",
    "type":"$type",
    "content":"$content"
}
        """.trimIndent()
    }
}