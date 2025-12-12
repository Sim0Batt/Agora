package server.models

import kotlinx.serialization.Serializable

@Serializable
class CreateNoteJson(
    val title: String,
    val type: String,
    val length: Int,
    val userId: Int,
) {
    override fun toString(): String {
        return """
{
    "title":"$title",
    "length":"$length",
    "type":"$type",
    "userId":"$userId",
}
        """.trimIndent()
    }
}