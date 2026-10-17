package script

import database.QueryManager
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import org.jetbrains.exposed.sql.Database
import java.io.File
import java.util.concurrent.TimeUnit

class LatexManager(val connection: Database) {

    fun saveLatexFile(noteId: Int): String{
        try{
            println("Started .tex file creation")
            val content: String = QueryManager.getLatexNoteContent(noteId)

            val noteInfo = QueryManager.getLatexNotesTitles(1).filter {
                it.id == noteId
            }[0]

            val filePath = "/home/gaia/notes/latex/${noteInfo.title}.tex"

            val latexFile = File(filePath)
            latexFile.createNewFile()
            latexFile.writeText(content)

            println("Latex file saved")

            return filePath
        }catch (e: Exception){
            println("Error during file creation: " + e.message)
            throw e
        }
    }


    fun generatePdf(filePath: String): File{
        try{
            println("Generating pdf")

            val documentTitle = File(filePath).nameWithoutExtension
            val pdfOutputDir = File("/home/gaia/notes/pdf/")
            if (!pdfOutputDir.exists()) {
                pdfOutputDir.mkdirs()
            }

            val pdfFile = File(pdfOutputDir, "$documentTitle.pdf")
            if (pdfFile.exists()) {
                pdfFile.delete()
            }

            val process = ProcessBuilder(
                "/usr/bin/pdflatex",
                "-interaction=nonstopmode",
                "-halt-on-error",
                "-output-directory", pdfOutputDir.absolutePath,
                filePath
            ).start()

            val finished = process.waitFor(180, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                process.waitFor(5, TimeUnit.SECONDS)
                throw Exception("Latex compile timed out")
            }

            val stdout = process.inputStream.bufferedReader().readText()
            val stderr = process.errorStream.bufferedReader().readText()

            if (pdfFile.exists() && pdfFile.length() > 0L) {
                if (process.exitValue() != 0) {
                    println(
                        "pdflatex returned non-zero but produced a PDF. " +
                                "exit=${process.exitValue()}\n$stdout\n$stderr"
                    )
                }
                return pdfFile
            }

            println("Error during latex compile:\n$stdout\n$stderr")
            throw Exception("Latex compile failed")
        }catch (e: Exception){
            println(e.message)
            throw e
        }
    }


    suspend fun saveImages(multiPartData: MultiPartData){
        try{
            var title: String? = null
            val allowedExtensions = setOf("jpg", "png", "img", "jpeg")
            val pendingFiles = mutableListOf<Pair<String, ByteArray>>()

            multiPartData.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        if (part.name == "title") {
                            title = part.value.trim()
                        }
                    }

                    is PartData.FileItem -> {
                        val originalName = part.originalFileName ?: ""
                        val extension = originalName.substringAfterLast('.', "").lowercase()

                        if (extension in allowedExtensions && originalName.isNotBlank()) {
                            @Suppress("DEPRECATION")
                            val fileBytes = part.streamProvider().use { it.readBytes() }
                            pendingFiles += originalName to fileBytes
                        }
                    }

                    else -> Unit
                }

                part.dispose()
            }

            val imagesDir = File("/home/gaia/img/$title/")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            pendingFiles.forEach { fileData ->
                val (originalName, fileBytes) = fileData
                val destination = File(imagesDir, originalName)
                destination.writeBytes(fileBytes)
            }
        }catch (e: Exception){
            println(e.message)
            throw e
        }
    }
}