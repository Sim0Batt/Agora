package org.example.agora.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

object DesktopLatexCodeConverter: LatexCodeConverter {
    override suspend fun codeConverter(title: String, latexCode: String): Boolean = withContext(Dispatchers.IO){
        val safeTitle = title.replace(" ", "_")

        val tmpDir = File(SettingsRepository().getTmpPath(""), safeTitle)
        val pdfDir = File(SettingsRepository().getPdfPath(""), safeTitle)

        if (!tmpDir.exists()) tmpDir.mkdirs()
        if (!pdfDir.exists()) pdfDir.mkdirs()

        val texFileName = "$safeTitle.tex"
        val texFile = File(tmpDir, texFileName)

        DesktopFileSaver.saveNoteToDisk(latexCode, texFile.absolutePath)

        val process = ProcessBuilder(
            "pdflatex",
            "-interaction=nonstopmode",
            "-halt-on-error",
            "-output-directory", pdfDir.absolutePath,
            texFile.absolutePath
        ).start()

        val finished = process.waitFor(5, TimeUnit.SECONDS)

        if (finished && process.exitValue() == 0) {
            return@withContext true
        } else {
            val error = process.inputStream.bufferedReader().readText()
            println("Error during latex compile:\n$error")
            return@withContext false
        }
    }

}