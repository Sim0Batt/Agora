package org.example.agora.utils

interface FileSaver {
    fun saveNoteToDisk(content: String, path: String)
    fun getFileText(path: String): String

    fun fileExists(path: String): Boolean
    fun deleteFile(path: String)
}