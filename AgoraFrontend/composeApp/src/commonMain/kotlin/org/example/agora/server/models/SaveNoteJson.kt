package org.example.agora.server.models

import kotlinx.serialization.Serializable

@Serializable
class SaveNoteJson(
    val noteId: Int,
    val length: Int,
    val content: String,
) {
    override fun toString(): String {
        return """
{
    "noteId":"$noteId",
    "length":"$length",
    "content":"$content"
}
        """.trimIndent()
    }
}