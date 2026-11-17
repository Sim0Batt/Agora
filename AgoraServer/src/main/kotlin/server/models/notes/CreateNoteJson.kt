package server.models.notes

import kotlinx.serialization.Serializable

@Serializable
class CreateNoteJson(
    val title: String,
    val type: String,
    val length: Int,
) {
    override fun toString(): String {
        return """
{
    "title":"$title",
    "length":"$length",
    "type":"$type",
}
        """.trimIndent()
    }
}