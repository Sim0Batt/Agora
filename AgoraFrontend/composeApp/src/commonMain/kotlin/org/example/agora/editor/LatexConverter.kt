package org.example.agora.editor

import kotlin.jvm.JvmStatic

object LatexConverter {
    fun convert(text: String): String {
        if (text.isEmpty()) {
            return text
        }

        var startedItemize = false
        var startedCode = false
        var convertedText = ""
        text.split("\n").forEach { line ->
            if(checkUpperCase(line) && line.isNotEmpty() && !line.contains("CODE")){
                convertedText += "\\subsection{${line.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}}"
            }else{
                if(line.split("").first().startsWith('-')){
                    if(!startedItemize){
                        convertedText += "\n\\begin{itemize}\n"
                        startedItemize = true
                    }
                    convertedText += formatItemize(formatItalicAndBold(line))
                }else if(startedItemize){
                    convertedText += "\\end{itemize}\n"
                    convertedText += formatItalicAndBold(line)
                    startedItemize = false
                }
                if(line.contains("CODE")){
                    if(!startedCode){
                        convertedText += "\n\\begin{lstlisting}\n"
                        startedCode = true
                    }else{
                        convertedText += "\n\\end{lstlisting}\n"
                        startedCode = false
                    }
                }

                else{
                    if(startedCode){
                        convertedText += line + "\n"
                    }else{
                        convertedText += formatItalicAndBold(line) + "\\\\ \n"
                    }
                }
            }
        }
        return convertedText
    }

    private fun checkUpperCase(line: String): Boolean{
        line.split(" ").forEach{word: String ->
            word.forEach{c: Char ->
                if(!c.isUpperCase()){
                    return false
                }
            }
        }
        return true
    }

    private fun formatItalicAndBold(text: String): String {
        var processed = text
        processed = processed.replace(Regex("\\[(.*?)\\]")) { matchResult ->
            "\\textit{${matchResult.groupValues[1]}}"
        }

        processed = processed.replace(Regex("(?<!\\.)\\b([A-Z0-9À-Ö]+(?:\\s+[A-Z0-9À-Ö]+)*)\\b")) { matchResult ->
            "\\textbf{${matchResult.value.lowercase()}}"
        }
        return processed
    }

    private fun formatItemize(text: String): String {
        return "	\\item ${text.substringAfter("-").substringBefore("\n")}\n"
    }

}