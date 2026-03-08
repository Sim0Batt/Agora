package org.example.agora.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object DesktopLatexCodeConverter: LatexCodeConverter {
    override suspend fun codeConverter(title: String, latexCode: String): Boolean = withContext(Dispatchers.IO){
        val tmpPath = "${SettingsRepository().getTmpPath("")}/$title.txt"
        DesktopFileSaver.saveNoteToDisk(latexCode, tmpPath)

        val process = ProcessBuilder(
            "/Library/TeX/texbin/pdflatex",
            "-interaction=nonstopmode",
            "-output-directory", SettingsRepository().getSavePathFlow(""),
            tmpPath
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