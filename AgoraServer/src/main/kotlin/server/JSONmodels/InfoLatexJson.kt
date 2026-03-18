package server.JSONmodels

import kotlinx.serialization.Serializable

@Serializable
class InfoLatexJson(
    val id: Int,
    val title: String,
) {
    override fun toString(): String {
        return """
{
    "id":"$id",
    "title":"$title"
}
        """.trimIndent()
    }
}