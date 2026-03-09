package server.JSONmodels

import kotlinx.serialization.Serializable

@Serializable
class AccountJson (
    val name: String,
    val id: Int
) {
    override fun toString(): String {
        return """
{
    "id":$id,
    "name":"$name"
}
        """.trimIndent()
    }
}