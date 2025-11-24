package org.example.agora.server

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.example.agora.server.models.NotesJson

object ServerRequests {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 5000 // 15 seconds
            connectTimeoutMillis = 5000 // 15 seconds
            socketTimeoutMillis = 5000 // 15 seconds
        }
    }

    const val URL = "0.0.0.0:8080"

    suspend fun getNotes(userId: String): List<NotesJson>{
        val url = "http://$URL/note/$userId"
        try {
            val rawResponse: String = client.get(url).bodyAsText()

            if (rawResponse.isBlank()) return emptyList()

            val trimmed = rawResponse.trim()

            val jsonContent = trimmed.replace(Regex("\\}\\s*\\{"), "},{")

            val jsonArrayString = "[$jsonContent]"

            return Json.decodeFromString<List<NotesJson>>(jsonArrayString)

        } catch (e: Exception) {
            println("Errore nel parsing: ${e.message}")
            return emptyList()
        }
    }
}