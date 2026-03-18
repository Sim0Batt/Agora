package org.example.agora.utils

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
                convertedText += "\n\\subsection{${line.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}}\n"
            }else{
                if(line.startsWith('-')){
                    if(!startedItemize){
                        convertedText += "\n\\begin{itemize}\n"
                        startedItemize = true
                    }
                    convertedText += formatItemize(formatItalicAndBold(line))
                }else if(startedItemize){
                    convertedText += "\\end{itemize}\n"
                    convertedText += formatItalicAndBold(line)
                    startedItemize = false
                }else if(line.trim() == "CODE"){
                    if(!startedCode){
                        convertedText += "\n\\begin{lstlisting}\n"
                        startedCode = true
                    }else{
                        convertedText += "\n\\end{lstlisting}\n"
                        startedCode = false
                    }
                } else{
                    convertedText += if(startedCode){
                        line + "\n"
                    }else{
                        formatItalicAndBold(line) + "\\\\ \n"
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

        processed = processed.replace(Regex("(?<!\\.)\\b([A-ZÀ-Ö]{2,}(?:\\s+[A-ZÀ-Ö]{2,})*)\\b")) { matchResult ->
            if(matchResult.value.length > 3){
                "\\textbf{${matchResult.value.lowercase()}}"
            }else{
                "\\textbf{${matchResult.value}}"
            }
        }
        return processed
    }

    private fun formatItemize(text: String): String {
        return "	\\item ${text.substringAfter("-").substringBefore("\n")}\n"
    }






}