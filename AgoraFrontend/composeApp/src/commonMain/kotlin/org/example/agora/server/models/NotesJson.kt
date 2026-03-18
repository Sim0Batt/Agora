package org.example.agora.server.models

import kotlinx.serialization.Serializable

@Serializable
class NotesJson(
    val id: Int,
    val title: String,
    val type: String,
    val length: Int,
    val userId: Int,
    val content: String = ""
) {
    override fun toString(): String {
        return """
{
    "id":"$id",
    "title":"$title",
    "length":"$length",
    "type":"$type",
    "userId":"$userId",
    "content":"$content"
}
        """.trimIndent()
    }
}