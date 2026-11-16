package server


import configuration.ReadXMLConfiguration
import database.QueryManager
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.application.install
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.partialcontent.PartialContent
import io.ktor.server.request.receive
import io.ktor.server.request.receiveText
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import it.tdlight.client.InputParameter
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.jdbc.Database
import script.LatexManager
import script.PlaylistManager
import script.SongsManager
import script.TelegramManager
import script.TodoManager
import server.models.misc.AccountJson
import server.models.notes.ContentJson
import server.models.notes.CreateNoteJson
import server.models.latexnotes.LatexNoteJson
import server.models.music.MusicSearchJson
import server.models.notes.SaveNoteJson
import server.models.TodoJson
import server.models.misc.UserJson
import server.models.music.CreatePlaylistJson
import java.io.File


val configuration = ReadXMLConfiguration.getConfiguration()

fun Application.module() {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(CORS) {
        allowHost("agora-5hgp.onrender.com", schemes = listOf("https"))
        allowHost("localhost:5173", schemes = listOf("http"))
        allowMethod(io.ktor.http.HttpMethod.Put)
        allowMethod(io.ktor.http.HttpMethod.Delete)
        allowHeader(HttpHeaders.ContentType)
    }
    install(PartialContent)
    install(AutoHeadResponse)


    val connection = Database.connect(
        url = configuration.databaseConfig!!.dbConnectionString,
        driver = configuration.databaseConfig.dbDriver,
        user = configuration.databaseConfig.dbUser,
        password = configuration.databaseConfig.dbPassword
    )

    val queryManager = QueryManager(connection)
    val todoManager = TodoManager(connection)
    val songsManager = SongsManager(connection)
    val tg = TelegramManager(configuration.telegram!!, connection, songsManager)
    val playlistManager = PlaylistManager(connection)


    tg.start()
    launch { runCatching { tg.connect() }.onFailure { log.error("Telegram", it) } }
    monitor.subscribe(ApplicationStopped) { tg.close() }

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


        get("/admin/telegram/status") { call.respond(tg.waitingFor().map { it.name }) }
        post("/admin/telegram/input/{param}") {
            val p = InputParameter.valueOf(call.parameters["param"]!!)
            call.respond(tg.provideInput(p, call.receiveText().trim()))
        }

        post("/music/search") {
            val input = call.receive<MusicSearchJson>()
            try{
                println("Started searching for: ${input.title}")
                call.respond(tg.search(input.title))
            }catch (e: Exception){
                call.respond(HttpStatusCode.BadRequest)
                error("Error during search of media ${e.message}: ${e.stackTraceToString()}")
            }
        }
        get("/music/{msg}/{idx}/download") {
            val messageId = call.parameters["msg"]
            val songId = call.parameters["idx"]
            val t = tg.pick(messageId!!.toLong(), songId!!.toInt())
            call.respond(t)
        }

        get("/songs"){
            try {
                call.respond(songsManager.getSongs())
            }catch (e: Exception){
                println(e.message)
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        post("/playlist"){
            try {
                val input = call.receive<CreatePlaylistJson>()
                playlistManager.createPlaylist(input.title)
                call.respond("OK")
            }catch (e: Exception){
                println(e.message)
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        get("/playlist"){
            call.respond(playlistManager.getPlaylists())
        }

        post("/playlist/add/{playlistId}/{songId}"){
            try {
                val playlistId = call.parameters["playlistId"]!!
                val songId = call.parameters["songId"]!!
                playlistManager.addSong(playlistId, songId)
                call.respond("OK")
            }catch (e: Exception){
                println(e.message)
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        get("/playlist/song/{playlistId}"){
            try {
                val playlistId = call.parameters["playlistId"]!!
                call.respond(playlistManager.getSongsForPlaylist(playlistId))
            }catch (e: Exception){
                println(e.message)
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        delete("/playlist/{playlistId}"){
            try {
                val playlistId = call.parameters["playlistId"]!!
                playlistManager.removePlaylist(playlistId)
                call.respond("OK")
            }catch (e: Exception){
                println(e.message)
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        delete("/playlist/{playlistId}/{songId}"){
            try {
                val playlistId = call.parameters["playlistId"]!!
                val songId = call.parameters["songId"]!!
                playlistManager.removeSongFromPlaylist(songId, playlistId)
                call.respond("OK")
            }catch (e: Exception){
                println(e.message)
                call.respond(HttpStatusCode.InternalServerError)
            }
        }


        get("/songs/{id}/stream") {
            val id = call.parameters["id"]!!
            val path = songsManager.getSongPath(id)
            val file = path?.let { File(it) }

            if (file == null || !file.isFile) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            val contentType = when (file.extension.lowercase()) {
                "mp3" -> ContentType.Audio.MPEG
                "ogg", "opus" -> ContentType.Audio.OGG
                "flac" -> ContentType("audio", "flac")
                "m4a", "aac" -> ContentType("audio", "mp4")
                "wav" -> ContentType("audio", "wav")
                else -> ContentType.Application.OctetStream
            }

            call.response.header(HttpHeaders.AcceptRanges, "bytes")
            call.response.header(HttpHeaders.CacheControl, "private, max-age=3600")
            call.respondFile(file)  // con PartialContent gestisce Range, 206 e Content-Length
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
