package server


import configuration.ReadXMLConfiguration
import database.QueryManager
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.application.install
import io.ktor.server.config.configLoaders
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.partialcontent.PartialContent
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.jdbc.Database
import script.DytManager
import script.LatexManager
import script.LoggerWriter
import script.PlaylistManager
import script.SongsManager
import script.TodoManager
import server.models.notes.ContentJson
import server.models.notes.CreateNoteJson
import server.models.latexnotes.LatexNoteJson
import server.models.music.MusicSearchJson
import server.models.notes.SaveNoteJson
import server.models.TodoJson
import server.models.music.CreatePlaylistJson
import server.models.music.DownloadSongJson
import server.models.music.SongJson
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

    val logger = LoggerWriter(configuration.log!!, "Agorà")

    val queryManager = QueryManager(connection, logger)
    val latexManager = LatexManager(connection, configuration.latex!!, logger)
    val todoManager = TodoManager(connection, logger)
    val songsManager = SongsManager(connection, logger)
    val playlistManager = PlaylistManager(connection, logger)
    val dytManager = DytManager(configuration.music!!.ydtEnginePath, configuration.music!!.saveFolder, logger)

    var display = false


    routing {
        get("/status"){
            call.respond("OK")
        }

        get("/display"){
            if(display){
                val process = withContext(Dispatchers.IO) {
                    ProcessBuilder("sudo", "-n", "systemctl", "restart", "display.service")
                        .redirectErrorStream(true)
                        .start()
                }
                val output = process.inputStream.bufferedReader().readText()
                val exitCode = withContext(Dispatchers.IO) {
                    process.waitFor()
                }
                if (exitCode != 0) {
                    logger.error("Display restart failed: $output")
                }
                display = !display
                logger.info("Switched display on")
                call.respondText(if (display) "OFF" else "ON")
            }else{
                val process = withContext(Dispatchers.IO) {
                    ProcessBuilder("sudo", "-n", "systemctl", "stop", "display.service")
                        .redirectErrorStream(true)
                        .start()
                }
                val output = process.inputStream.bufferedReader().readText()
                val exitCode = withContext(Dispatchers.IO) {
                    process.waitFor()
                }
                if (exitCode != 0) {
                    logger.error("Display stop failed: $output")
                }
                display = !display
                logger.info("Switched display on")
                call.respondText(if (display) "OFF" else "ON")
            }
        }


        //Notes Management
        get("/note") {
            val result = queryManager.getNotes().joinToString(";\n")
            logger.info(result)
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
            logger.info(queryManager.getNotesContent(noteId))
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
                if (e is CancellationException) {
                    currentCoroutineContext().ensureActive()
                }
                logger.error(e.message!!)
                call.respondText("failure")
            }
        }

        get("/latex/list"){
            try {
                val result = queryManager.getLatexNotesTitles()
                logger.info(result.joinToString("\n"))
                call.respond(result)
            }catch (e: Exception){
                if (e is CancellationException) {
                    currentCoroutineContext().ensureActive()
                }
                logger.error(e.message!!)
                call.respondText("failure")
            }
        }

        get ("/latex/{id}"){
            val noteId = call.parameters["id"]?.toIntOrNull() ?: -1
            try {
                call.respondText(queryManager.getLatexNoteContent(noteId))
            }catch (e: Exception){
                if (e is CancellationException) {
                    currentCoroutineContext().ensureActive()
                }
                logger.error(e.message!!)
                call.respondText("failure")
            }
        }

        delete ("/latex/{id}"){
            val noteId = call.parameters["id"]?.toIntOrNull() ?: -1
            try{
                queryManager.deleteLatexNote(noteId)
                call.respondText("success")
            }catch (e: Exception){
                if (e is CancellationException) {
                    currentCoroutineContext().ensureActive()
                }
                logger.error(e.message!!)
                call.respondText("failure")
            }
        }

        post ("/latex/update"){
            val notesJson = call.receive<SaveNoteJson>()
            try{
                queryManager.updateLatexNote(notesJson)
                call.respondText("Note updated")
            }catch (e: Exception){
                if (e is CancellationException) {
                    currentCoroutineContext().ensureActive()
                }

                logger.error(e.message!!)
                call.respondText("failure")
            }
        }

        post("/latex/download/{noteId}"){
            try{
                val noteId = call.parameters["noteId"]?.toIntOrNull() ?: -1
                val filePath = latexManager.saveLatexFile(noteId)
                val pdf = latexManager.generatePdf(filePath)
                call.respondFile(pdf)
            }catch (e: Exception){
                if (e is CancellationException) {
                    currentCoroutineContext().ensureActive()
                }

                logger.error(e.message!!)
                call.respondText("failure")
            }
        }

        post("/latex/image"){
            try{
                val multipartData = call.receiveMultipart()
                latexManager.saveImages(multipartData)
                call.respondText("success")
            }catch (e: Exception){
                if (e is CancellationException) {
                    currentCoroutineContext().ensureActive()
                }
                logger.error(e.message!!)
                call.respondText("failure")
            }
        }

        get("/todo"){
            try {
                call.respond(todoManager.getTodos())
            }catch (e: Exception){
                if (e is CancellationException) {
                    currentCoroutineContext().ensureActive()
                }
                logger.error(e.message!!)
                call.respond("Error during todo retrieve")
            }
        }

        post("/todo") {
            try {
                val todoJson = call.receive<TodoJson>()
                todoManager.addTodo(todoJson)
                call.respond(HttpStatusCode.OK)
            }catch (e: Exception){
                if (e is CancellationException) {
                    currentCoroutineContext().ensureActive()
                }
                logger.error(e.message!!)
                call.respond("Error during todo creation")
            }
        }

        delete("/todo/{todoId}"){
            try {
                val todoId = call.parameters["todoId"]
                todoManager.removeTodo(todoId!!.toInt())
                call.respond(HttpStatusCode.OK)
            }catch (e: Exception){
                if (e is CancellationException) {
                    currentCoroutineContext().ensureActive()
                }
                call.respond("Error during todo delete")
            }
        }

        get("/todo/emacs"){
            try{
                call.respond(todoManager.getOrgTodos())
            }catch (e: Exception){
                if (e is CancellationException) {
                    currentCoroutineContext().ensureActive()
                }
                call.respond(e.message!!)
            }
        }

        post("/music/search") {
            val input = call.receive<MusicSearchJson>()
            try{
                logger.info("Started searching for: ${input.title}")
                call.respond(dytManager.search(input.title))
            }catch (e: Exception){
                if (e is CancellationException) {
                    currentCoroutineContext().ensureActive()
                }
                call.respond(HttpStatusCode.BadRequest)
                logger.error("Error during search of media ${e.message}: ${e.stackTraceToString()}")
            }
        }
        post("/music/download") {
            val input = call.receive<SongJson>()
            try {
                dytManager.download(input.url)
                songsManager.saveSong(input.title, input.author, input.duration, "${configuration.music!!.saveFolder}/${input.id}.webm")
                call.respond("OK")
            } catch (e: Exception) {
                if (e is CancellationException) {
                    currentCoroutineContext().ensureActive()
                }
                logger.error(e.message!!)
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Download fallito")
            }
        }

        delete("/song/{songId}"){
            val songId = call.parameters["songId"]!!
            try {
                songsManager.deleteSong(songId)
                call.respond("OK")
            } catch (e: Exception){
                if (e is CancellationException) {
                    currentCoroutineContext().ensureActive()
                }
                logger.error(e.message!!)
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Download fallito")
            }
        }

        get("/songs"){
            try {
                call.respond(songsManager.getSongs())
            }catch (e: Exception){
                if (e is CancellationException) {
                    currentCoroutineContext().ensureActive()
                }
                logger.error(e.message!!)
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        post("/playlist"){
            try {
                val input = call.receive<CreatePlaylistJson>()
                playlistManager.createPlaylist(input.title)
                call.respond("OK")
            }catch (e: Exception){
                if (e is CancellationException) {
                    currentCoroutineContext().ensureActive()
                }
                logger.error(e.message!!)
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
                if (e is CancellationException) {
                    currentCoroutineContext().ensureActive()
                }
                logger.error(e.message!!)
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        get("/playlist/song/{playlistId}"){
            try {
                val playlistId = call.parameters["playlistId"]!!
                call.respond(playlistManager.getSongsForPlaylist(playlistId))
            }catch (e: Exception){
                if (e is CancellationException) {
                    currentCoroutineContext().ensureActive()
                }
                logger.error(e.message!!)
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        delete("/playlist/{playlistId}"){
            try {
                val playlistId = call.parameters["playlistId"]!!
                playlistManager.removePlaylist(playlistId)
                call.respond("OK")
            }catch (e: Exception){
                if (e is CancellationException) {
                    currentCoroutineContext().ensureActive()
                }
                logger.error(e.message!!)
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
                if (e is CancellationException) {
                    currentCoroutineContext().ensureActive()
                }
                logger.error(e.message!!)
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        post("/music/import"){
            val input = call.receive<DownloadSongJson>()
            try {
                val songs = dytManager.import(input.url)
                val playlistId = playlistManager.createPlaylist(input.title)
                val count = songs.size
                var counter = 0
                songs.forEach { song: SongJson ->
                    dytManager.download(song.url)
                    val songId = songsManager.saveSong(song.title, song.author, song.duration, "${configuration.music!!.saveFolder}/${song.id}.webm")
                    playlistManager.addSong(playlistId, songId)
                    counter++
                    logger.info("Imported song $counter of $count")
                }
                call.respond("OK")
            }catch (e: Exception){
                if (e is CancellationException) {
                    currentCoroutineContext().ensureActive()
                }
                logger.error(e.message!!)
                call.respond(HttpStatusCode.InternalServerError)
            }
        }



        get("/songs/{id}/stream") {
            val id = call.parameters["id"]!!
            val path = songsManager.getSongPath(id)
            val file = path.let { File(it) }

            if (!file.isFile) {
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
