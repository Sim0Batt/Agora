package org.example.agora.server.models

import kotlinx.serialization.Serializable

@Serializable
class LatexNoteJson(
    val title: String,
    val content: String,
    val userId: Int
) {
    override fun toString(): String {
        return """
{
"title":"$title",
"content":"$content",
"userId":"$userId"
}
        """.trimIndent()
    }
}