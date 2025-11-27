package org.example.agora

import org.example.agora.settings.FileSaver
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.createFile

object DesktopFileSaver: FileSaver {

    override fun getFileText(path: String): String {
        return if(File(path).exists()) {
            File(path).readText()
        }else{
            ""
        }

    }

    override fun saveNoteToDisk(content: String, path: String) {
        if(!File(path).exists()) {
            Path(path).createFile()
        }
        File(path).writeText(content)
    }
}