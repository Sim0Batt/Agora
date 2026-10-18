package server


import configuration.ReadXMLConfiguration
import database.QueryManager
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.application.install
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.Query
import script.LatexManager
import script.TodoManager
import server.JSONmodels.AccountJson
import server.JSONmodels.ContentJson
import server.JSONmodels.CreateNoteJson
import server.JSONmodels.LatexNoteJson
import server.JSONmodels.SaveNoteJson
import server.JSONmodels.TodoJson
import server.JSONmodels.UserJson



val configuration = ReadXMLConfiguration.getConfiguration()

fun Application.module() {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }


    val connection = Database.connect(
        url = configuration.databaseConfig!!.dbConnectionString,
        driver = configuration.databaseConfig.dbDriver,
        user = configuration.databaseConfig.dbUser,
        password = configuration.databaseConfig.dbPassword
    )

    val queryManager = QueryManager(connection)
    val latexManager = LatexManager(connection)
    val todoManager = TodoManager(connection)

    routing {
        //Users Management
        get("/user") {
            call.respondText(queryManager.getUsers().joinToString(";\n"))
        }
        post("/user") {
            val credentials = call.receive<UserJson>()
            queryManager.createUser(credentials.name, credentials.password)
            call.respondText("User created")
        }

        post("/login") {
            val credentials = call.receive<UserJson>()
            val account = queryManager.loginUser(credentials)
            if(account == AccountJson("null", -1)){
                call.respondText("failure")
            }else{
                call.respondText(account.toString())
            }
        }

        get("/user/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: -1
            val account = queryManager.getSpecificUser(id)
            call.respondText(account.toString())
        }


        //Notes Management
        get("/note") {
            val result = queryManager.getNotes().joinToString(";\n")
            println(result)
            call.respondText(result)
        }

        get("/note/{id}") {
            val userId = call.parameters["id"].toString().toInt()
            val result = queryManager.getUserNotes(userId).joinToString(";\n")
            call.respondText(result)
        }

        delete("/note/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: -1
            queryManager.deleteNote(id)
            call.respondText("Note deleted")
        }

        post("/note") {
            val notesJson = call.receive<CreateNoteJson>()
            queryManager.createNote(notesJson)
            call.respondText("Note created")
        }

        post("/updateNote"){
            val notesJson = call.receive<SaveNoteJson>()
            queryManager.updateNote(notesJson)
            call.respondText("Note updated")
        }

        post("/content/{id}"){
            val noteId = call.parameters["id"]?.toIntOrNull() ?: -1
            println(queryManager.getNotesContent(noteId))
            call.respondText(
                ContentJson(
                    queryManager.getNotesContent(noteId),
                ).toString()
            )
        }

        post ("/latex"){
            val input = call.receive<LatexNoteJson>()
            try {
                val noteId = queryManager.createLatexNote(input)
                call.respond(noteId)
            }catch (e: Exception){
                println(e.stackTraceToString())
                call.respondText("failure")
            }
        }

        get("/latex/list/{userId}"){
            val userId = call.parameters["userId"]?.toIntOrNull() ?: -1
            try {
                val result = queryManager.getLatexNotesTitles(userId)
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
                call.respondText(queryManager.getLatexNoteContent(noteId))
            }catch (e: Exception){
                println(e.stackTraceToString())
                call.respondText("failure")
            }
        }

        delete ("/latex/{id}"){
            val noteId = call.parameters["id"]?.toIntOrNull() ?: -1
            try{
                queryManager.deleteLatexNote(noteId)
                call.respondText("success")
            }catch (e: Exception){
                println(e.stackTraceToString())
                call.respondText("failure")
            }
        }

        post ("/latex/update"){
            val notesJson = call.receive<SaveNoteJson>()
            try{
                queryManager.updateLatexNote(notesJson)
                call.respondText("Note updated")
            }catch (e: Exception){
                println(e.stackTraceToString())
                call.respondText("failure")
            }
        }

        get("/todo"){
            try {
                call.respond(todoManager.getTodos())
            }catch (e: Exception){
                call.respond("Error during todo retrieve")
            }
        }

        post("/todo") {
            try {
                val todoJson = call.receive<TodoJson>()
                todoManager.addTodo(todoJson)
                call.respond(HttpStatusCode.OK)
            }catch (e: Exception){
                call.respond("Error during todo creation")
            }
        }

        delete("/todo/{todoId}"){
            try {
                val todoId = call.parameters["todoId"]
                todoManager.removeTodo(todoId!!.toInt())
                call.respond(HttpStatusCode.OK)
            }catch (e: Exception){
                call.respond("Error during todo delete")
            }
        }

        get("/todo/emacs"){
            try{
                call.respond(todoManager.getOrgTodos())
            }catch (e: Exception){
                call.respond("Error during Org Extraction")
            }
        }


    }

}

class ServerConfig {
    fun run() {
        embeddedServer(
            Netty,
            port = configuration.server!!.port.toInt() ?: 18035,
            host = configuration.server!!.host,
            module = Application::module
        ).start(wait = true)
    }
}
