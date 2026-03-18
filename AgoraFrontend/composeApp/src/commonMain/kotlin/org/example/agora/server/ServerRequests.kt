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
import org.example.agora.server.models.ContentJson
import org.example.agora.server.models.CreateNoteJson
import org.example.agora.server.models.InfoLatexJson
import org.example.agora.server.models.LatexNoteJson
import org.example.agora.server.models.NotesJson
import org.example.agora.server.models.SaveNoteJson
import org.example.agora.utils.FileSaver
import org.example.agora.utils.SettingsRepository

class ServerRequests (settingsRepository: SettingsRepository){
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 10000
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 10000
        }
    }

    val URL = "https://capitalist-nessa-simonorg-57c9d5ca.koyeb.app" // 13.60.112.248


    suspend fun getNotes(userId: String, fileSaver: FileSaver): List<NotesJson>{
        val url = "$URL/note/$userId"

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
        val url = "$URL/note/$noteId"

        try {
            client.delete(url).bodyAsText()
            return "success"
        } catch (e: Exception) {
            println("Errore nella rimozione della Nota: ${e.message}")
            return "failure"
        }
    }

    suspend fun createNote(createNoteJson: CreateNoteJson): String{
        val url = "$URL/note"

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

    suspend fun updateNote(saveNoteJson: SaveNoteJson): String{
        val url = "$URL/updateNote"
        try{
            client.post(url){
                contentType(ContentType.Application.Json)
                setBody(saveNoteJson)
            }
            return "success"
        }catch (e: Exception){
            println("Errore nella rimozione della Nota: ${e.message}")
            return "failure"
        }
    }


    suspend fun getNotesContent(noteId: Int): String{
        val url = "$URL/content/$noteId"
        try{
            val response = Json.decodeFromString<ContentJson>(client.post(url).bodyAsText())
            return response.content
        }catch (e: Exception){
            println("Errore nella rimozione della Nota: ${e.message}")
            return "failure"
        }
    }

    suspend fun createLatexNote(title: String, userId: Int): Int{
        val url = "$URL/latex"
        val latexJson = LatexNoteJson(title, "", userId)
        try{
            val latexNoteId = client.post(url){
                contentType(ContentType.Application.Json)
                setBody(latexJson)
            }
            return latexNoteId.toString().toInt()
        }catch (e: Exception){
            println("Error during latex note creation: ${e.message}")
            return -1
        }
    }

    suspend fun getLatexContent(noteId: Int): String{
        val url = "$URL/latex/$noteId"
        try{
            val response = client.get(url).bodyAsText()
            return response
        }catch (e: Exception){
            println("Error during latex note creation: ${e.message}")
            return "failure"
        }
    }

    suspend fun deleteLatexNote(noteId: Int): String{
        val url = "$URL/latex/$noteId"
        try{
            client.delete(url).bodyAsText()
            return "success"
        }catch (e: Exception){
            println("Error during latex note delete: ${e.message}")
            return "failure"
        }
    }

    suspend fun getLatexNotesList(userId: Int): List<InfoLatexJson>{
        val url = "$URL/latex/list/$userId"
        try{
            val response = client.get(url).bodyAsText()
            return Json.decodeFromString<List<InfoLatexJson>>(response)
        }catch (e: Exception){
            println("Error during latex note list: ${e.message}")
            return emptyList()
        }
    }

    suspend fun updateLatexNote(noteId: Int, content: String){
        val url = "$URL/latex/update"
        val contentJson = SaveNoteJson(noteId, 0, content)
        try{
            client.post(url){
                contentType(ContentType.Application.Json)
                setBody(contentJson)
            }
        }catch (e: Exception){
            println("Error during latex note update: ${e.message}")
        }
    }



}