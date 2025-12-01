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

    override fun fileExists(path: String): Boolean {
        return File(path).exists()
    }

    override fun deleteFile(path: String) {
        File(path).delete()
    }

    override fun saveNoteToDisk(content: String, path: String) {
        if(!File(path).exists()) {
            Path(path).createFile()
        }
        File(path).writeText(content)
    }



}