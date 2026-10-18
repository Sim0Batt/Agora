package server.JSONmodels

import kotlinx.serialization.Serializable

@Serializable
class TodoJson(
    val title: String,
    val description: String,
    val expirationDate: String,
    val id: Int? = null,
    val type: String
) {
    override fun toString(): String {
        return """
{
    "title": "$title",
    "description": "$description",
    "expirationDate": "$expirationDate",
    "type": "$type"
}
        """.trimIndent()
    }
}