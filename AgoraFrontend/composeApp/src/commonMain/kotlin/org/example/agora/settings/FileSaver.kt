package org.example.agora.settings

interface FileSaver {
    fun saveNoteToDisk(content: String, path: String)
    fun getFileText(path: String): String
}