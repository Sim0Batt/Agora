package server.models.latexnotes

import kotlinx.serialization.Serializable

@Serializable
class LatexNoteJson(
    val title: String,
    val content: String,
    val type: String
) {
    override fun toString(): String {
        return """
{
"title":"$title",
"content":"$content",
"type":"$type"
}
        """.trimIndent()
    }
}