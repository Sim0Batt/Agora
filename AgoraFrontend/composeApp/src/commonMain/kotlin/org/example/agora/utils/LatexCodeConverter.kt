package org.example.agora.utils

interface LatexCodeConverter {
    suspend fun codeConverter(title: String, latexCode: String): Boolean
}