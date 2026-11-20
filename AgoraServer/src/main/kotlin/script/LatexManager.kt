package script

import configuration.LatexConfiguration
import database.QueryManager
import database.tables.LatexNotesTable
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.io.File
import java.util.concurrent.TimeUnit

class LatexManager(val connection: Database, val latexConf: LatexConfiguration, val logger: LoggerWriter) {
    private val queryManager = QueryManager(connection, logger)

    fun saveLatexFile(noteId: Int): String{
        try{
            logger.info("Started .tex file creation")
            val content: String = queryManager.getLatexNoteContent(noteId)

            val noteInfo = queryManager.getLatexNotesTitles().filter {
                it.id == noteId
            }[0]

            val filePath = "${latexConf.cachePath}/${noteInfo.title}.tex"

            val latexFile = File(filePath)
            latexFile.createNewFile()
            latexFile.writeText(content)

            logger.info("Latex file saved")

            return filePath
        }catch (e: Exception){
            throw Exception("Error during file creation ${e.message}: ${e.stackTraceToString()}")
        }
    }


    fun generatePdf(filePath: String): File{
        try{
            logger.info("Generating pdf")

            val documentTitle = File(filePath).nameWithoutExtension
            val pdfOutputDir = File(latexConf.pdfPath)
            if (!pdfOutputDir.exists()) {
                pdfOutputDir.mkdirs()
            }

            val pdfFile = File(pdfOutputDir, "$documentTitle.pdf")
            if (pdfFile.exists()) {
                pdfFile.delete()
            }

            val process = ProcessBuilder(
                latexConf.engine,
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
                    logger.info(
                        "pdflatex returned non-zero but produced a PDF. " +
                                "exit=${process.exitValue()}\n$stdout\n$stderr"
                    )
                }
                return pdfFile
            }

            logger.error("Error during latex compile:\n$stdout\n$stderr")
            throw Exception("Latex compile failed: \n$stdout\n$stderr\"")
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

            val imagesDir = File("${latexConf.imgPath}/$title/")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            pendingFiles.forEach { fileData ->
                val (originalName, fileBytes) = fileData
                val destination = File(imagesDir, originalName)
                destination.writeBytes(fileBytes)
            }

            logger.info("Images saved")
        }catch (e: Exception){
            throw Exception("Error during images save ${e.message}: ${e.stackTraceToString()}")
        }
    }

    fun getSavedImages(title: String): List<String>{
        try {
            val imgsDir = File("${latexConf.imgPath}/$title")
            val filesList = mutableListOf<String>()
            imgsDir.listFiles().forEach {
                filesList.add(it.name)
            }

            return filesList
        }catch (e: Exception){
            throw Exception("Error during images retrieve ${e.message}: ${e.stackTraceToString()}")
        }
    }

    fun deleteLatexNote(title: String){
        try {
            transaction(connection){
                LatexNotesTable.deleteWhere {
                    LatexNotesTable.title eq title
                }
            }
            File(latexConf.cachePath + "/${title}.tex").delete()
            File(latexConf.pdfPath + "/${title}.pdf").delete()
            File(latexConf.imgPath + "/$title").deleteRecursively()
        }catch (e: Exception){
            throw Exception("Error while deleting note $title ${e.message}: ${e.stackTraceToString()}")
        }
    }

}