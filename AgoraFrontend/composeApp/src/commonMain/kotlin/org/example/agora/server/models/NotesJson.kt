package org.example.agora.server.models

import kotlinx.serialization.Serializable

@Serializable
class NotesJson(
    val id: Int,
    val title: String,
    val type: String,
    val length: Int,
    val savedPath: String,
    val userId: Int,
) {
    override fun toString(): String {
        return """
{
    "id":"$id",
    "title":"$title",
    "length":"$length",
    "type":"$type",
    "savedPath":"$savedPath",
    "userId":"$userId"
}
        """.trimIndent()
    }
}