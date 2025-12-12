package org.example.agora.server.models

import kotlinx.serialization.Serializable

@Serializable
class ContentJson(
    val content: String
) {
    override fun toString(): String {
        return """
{
    "content":"$content"
}
        """.trimIndent()
    }
}