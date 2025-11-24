package org.example.agora

import org.example.agora.settings.FileSaver
import java.io.File

object DesktopFileSaver: FileSaver {

    override fun getFileText(path: String): String {
        return File(path).readText()
    }

    override fun saveNoteToDisk(content: String, path: String) {
        File(path).writeText(content)
    }
}