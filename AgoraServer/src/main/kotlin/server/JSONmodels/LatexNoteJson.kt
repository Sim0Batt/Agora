package server.JSONmodels

import kotlinx.serialization.Serializable

@Serializable
class LatexNoteJson(
    val title: String,
    val content: String,
    val userId: Int,
    val type: String
) {
    override fun toString(): String {
        return """
{
"title":"$title",
"content":"$content",
"userId":"$userId",
"type":"$type"
}
        """.trimIndent()
    }
}