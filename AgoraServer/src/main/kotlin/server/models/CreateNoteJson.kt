package server.models

import kotlinx.serialization.Serializable

@Serializable
class CreateNoteJson(
    val title: String,
    val type: String,
    val length: Int,
    val userId: Int,
    val path: String = ""
) {
    override fun toString(): String {
        return """
{
    "title":"$title",
    "length":"$length",
    "type":"$type",
    "path":"$path",
    "userId":"$userId"
}
        """.trimIndent()
    }
}