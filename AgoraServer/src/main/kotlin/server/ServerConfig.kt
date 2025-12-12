package server


import database.QueryManager
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.application.install
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.receive
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import server.models.AccountJson
import server.models.ContentJson
import server.models.CreateNoteJson
import server.models.SaveNoteJson
import server.models.UserJson


fun Application.module() {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }

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
            println(QueryManager.getContent(noteId))
            call.respondText(
                ContentJson(
                    QueryManager.getContent(noteId),
                ).toString()
            )
        }

    }

}

class ServerConfig {
    fun run() {
        embeddedServer(
            Netty,
            port = 8080,
            host = "0.0.0.0",
            module = Application::module
        ).start(wait = true)
    }
}
