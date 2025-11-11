package server.models

import kotlinx.serialization.Serializable

@Serializable
class UserJson(
    val name: String,
    val password: String,
) {
    override fun toString(): String {
        return """
{
    "name":"$name",
    "password":"$password"
}
        """.trimIndent()
    }
}