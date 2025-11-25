package org.example.agora.server

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.example.agora.server.models.CreateNoteJson
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
            requestTimeoutMillis = 5000
            connectTimeoutMillis = 5000
            socketTimeoutMillis = 5000
        }
    }
    const val URL = "13.60.112.248:8080"
    suspend fun getNotes(userId: String): List<NotesJson>{
        val url = "http://$URL/note/$userId"

        try {
            val rawResponse: String = client.get(url).bodyAsText()

            val jsonStrings: List<String> = rawResponse.split(";\n")

            val parsedStrings: List<String> = jsonStrings.map { it.replace("\\", "\\\\") }

            val notes: List<NotesJson> = parsedStrings
                .filter { it.isNotBlank() }
                .map { jsonString ->
                    Json.decodeFromString<NotesJson>(jsonString)
                }

            return notes
        } catch (e: Exception) {
            println("Errore nel parsing del NotesJson: ${e.message}")
            return emptyList()
        }
    }

    suspend fun deleteNote(noteId: Int): String{
        val url = "http://$URL/note/$noteId"

        try {
            client.delete(url).bodyAsText()
            return "success"
        } catch (e: Exception) {
            println("Errore nella rimozione della Nota: ${e.message}")
            return "failure"
        }
    }

    suspend fun createNote(createNoteJson: CreateNoteJson): String{
        val url = "http://$URL/note"

        try {
            client.post(url){
                contentType(ContentType.Application.Json)
                setBody(createNoteJson)
            }
            return "success"
        } catch (e: Exception) {
            println("Errore nella rimozione della Nota: ${e.message}")
            return "failure"
        }
    }
}