package server


import database.DatabaseConfig
import database.QueryManager
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.application.install
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import io.netty.handler.codec.http.HttpStatusClass
import kotlinx.serialization.json.Json
import script.LatexManager
import server.JSONmodels.AccountJson
import server.JSONmodels.ContentJson
import server.JSONmodels.CreateNoteJson
import server.JSONmodels.LatexNoteJson
import server.JSONmodels.SaveNoteJson
import server.JSONmodels.UserJson
import java.io.File


fun Application.module() {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }

    val latexManager = LatexManager(connection = DatabaseConfig.getConnection())

    routing {
        //Users Management
        get("/user") {
            call.respondText(QueryManager.getUsers().joinToString(";\n"))
        }
        post("/user") {
            val credentials = call.receive<UserJson>()
            QueryManager.createUser(credentials.name, credentials.password)
            call.respondText("User created")
        }

        post("/login") {
            val credentials = call.receive<UserJson>()
            val account = QueryManager.loginUser(credentials)
            if(account == AccountJson("null", -1)){
                call.respondText("failure")
            }else{
                call.respondText(account.toString())
            }
        }

        get("/user/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: -1
            val account = QueryManager.getSpecificUser(id)
            call.respondText(account.toString())
        }


        //Notes Management
        get("/note") {
            val result = QueryManager.getNotes().joinToString(";\n")
            println(result)
            call.respondText(result)
        }

        get("/note/{id}") {
            val userId = call.parameters["id"].toString().toInt()
            val result = QueryManager.getUserNotes(userId).joinToString(";\n")
            call.respondText(result)
        }

        delete("/note/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: -1
            QueryManager.deleteNote(id)
            call.respondText("Note deleted")
        }

        post("/note") {
            val notesJson = call.receive<CreateNoteJson>()
            QueryManager.createNote(notesJson)
            call.respondText("Note created")
        }

        post("/updateNote"){
            val notesJson = call.receive<SaveNoteJson>()
            QueryManager.updateNote(notesJson)
            call.respondText("Note updated")
        }

        post("/content/{id}"){
            val noteId = call.parameters["id"]?.toIntOrNull() ?: -1
            println(QueryManager.getNotesContent(noteId))
            call.respondText(
                ContentJson(
                    QueryManager.getNotesContent(noteId),
                ).toString()
            )
        }

        post ("/latex"){
            val input = call.receive<LatexNoteJson>()
            try {
                val noteId = QueryManager.createLatexNote(input)
                call.respond(noteId)
            }catch (e: Exception){
                println(e.stackTraceToString())
                call.respondText("failure")
            }
        }

        get("/latex/list/{userId}"){
            val userId = call.parameters["userId"]?.toIntOrNull() ?: -1
            try {
                val result = QueryManager.getLatexNotesTitles(userId)
                println(result)
                call.respond(result)
            }catch (e: Exception){
                println(e.stackTraceToString())
                call.respondText("failure")
            }
        }

        get ("/latex/{id}"){
            val noteId = call.parameters["id"]?.toIntOrNull() ?: -1
            try {
                call.respondText(QueryManager.getLatexNoteContent(noteId))
            }catch (e: Exception){
                println(e.stackTraceToString())
                call.respondText("failure")
            }
        }

        delete ("/latex/{id}"){
            val noteId = call.parameters["id"]?.toIntOrNull() ?: -1
            try{
                QueryManager.deleteLatexNote(noteId)
                call.respondText("success")
            }catch (e: Exception){
                println(e.stackTraceToString())
                call.respondText("failure")
            }
        }

        post ("/latex/update"){
            val notesJson = call.receive<SaveNoteJson>()
            try{
                QueryManager.updateLatexNote(notesJson)
                call.respondText("Note updated")
            }catch (e: Exception){
                println(e.stackTraceToString())
                call.respondText("failure")
            }
        }

        get("/todo"){
            try {
                call.respond(QueryManager.getTodos())
            }catch (e: Exception){
                call.respond("Error during todo retrieve")
            }
        }

    }

}

class ServerConfig {
    fun run() {
        embeddedServer(
            Netty,
            port = System.getenv("PORT")?.toIntOrNull() ?: 18035,
            host = "0.0.0.0",
            module = Application::module
        ).start(wait = true)
    }
}
