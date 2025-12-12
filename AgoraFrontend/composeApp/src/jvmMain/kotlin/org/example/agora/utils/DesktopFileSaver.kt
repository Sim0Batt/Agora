package org.example.agora.utils

import java.io.File
import java.io.IOException
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
        val target = File(path)

        val parent = target.parentFile
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                throw IOException("Cannot create directory: ${parent.absolutePath}")
            }
        }

        if (target.exists() && target.isDirectory) {
            throw IOException("Target path is a directory, expected file: ${target.absolutePath}")
        }

        target.writeText(content)
    }



}