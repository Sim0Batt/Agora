package org.example.agora.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object DesktopLatexCodeConverter: LatexCodeConverter {
    override suspend fun codeConverter(title: String, latexCode: String): Boolean = withContext(Dispatchers.IO){
        val savePath = "${SettingsRepository().getSavePathFlow("")}/$title.tex"
        DesktopFileSaver.saveNoteToDisk(latexCode, savePath)

        val process = ProcessBuilder(
            "pdflatex",
            "-interaction=nonstopmode",
            "-output-directory", SettingsRepository().getSavePathFlow(""),
            savePath
        ).start()

        val finished = process.waitFor(5, TimeUnit.SECONDS)

        if (finished && process.exitValue() == 0) {
            // Successo! Il PDF si chiamerà document.pdf
            return@withContext true
        } else {
            // Leggi gli errori (utile per debug)
            val error = process.inputStream.bufferedReader().readText()
            println("Errore compilazione LaTeX:\n$error")
            return@withContext false
        }
    }

}