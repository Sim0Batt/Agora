package script

import kotlinx.serialization.json.Json
import server.models.music.SongJson
import java.io.BufferedReader
import java.io.InputStreamReader

class DytManager (val enginePath: String, val saveFolder: String, val logger: LoggerWriter){
    fun search(title: String): List<SongJson>{
        logger.info("Started search for song $title")
        var errorReader: BufferedReader? = null
        try{
            val process = ProcessBuilder(
                "/usr/bin/python",
                enginePath + "yt_search.py",
                title
            ).redirectErrorStream(true).start()

            val reader = BufferedReader(InputStreamReader(process!!.inputStream))
            errorReader = BufferedReader(InputStreamReader(process.errorStream))

            val outputString = reader.readText()

            return Json.decodeFromString<List<SongJson>>(outputString)
        }catch (e: Exception){
            throw Exception("Error during search ${e.message}: ${e.stackTraceToString()}\nPython log: ${errorReader!!.readText()}")
        }
    }

    fun download(url: String){
        logger.info("Started download for song url $url")
        var errorReader: BufferedReader? = null
        try{
            val process = ProcessBuilder(
                "/usr/bin/python",
                enginePath + "yt_download.py",
                url,
                saveFolder
            ).redirectErrorStream(true).start()

            val reader = BufferedReader(InputStreamReader(process!!.inputStream))
            errorReader = BufferedReader(InputStreamReader(process.errorStream))

            val outputString = reader.readText()
            if(outputString == "FAILURE") throw Exception("Python Code Error")
        }catch (e: Exception){
            throw Exception("Error during search ${e.message}: ${e.stackTraceToString()}\nPython log: ${errorReader!!.readText()}")
        }
    }

    fun import(url: String): List<SongJson>{
        var errorReader: BufferedReader? = null
        logger.info("Started import of playlist")
        try{
            val process = ProcessBuilder(
                "/usr/bin/python",
                enginePath + "yt_playlist.py",
                url
            ).redirectErrorStream(true).start()

            val reader = BufferedReader(InputStreamReader(process!!.inputStream))
            errorReader = BufferedReader(InputStreamReader(process.errorStream))

            val outputString = reader.readText()
            return Json.decodeFromString<List<SongJson>>(outputString)

        }catch (e: Exception){
            throw Exception("Error during search ${e.message}: ${e.stackTraceToString()}\nPython log: ${errorReader!!.readText()}")
        }
    }
}