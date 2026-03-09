package org.example.agora.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class LatexSyntaxTransformation(
    private val commandColor: Color,
    private val bracketColor: Color
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val inputText = text.text

        val annotatedString = buildAnnotatedString {
            append(inputText)

            // 1. Parentesi Graffe { } e Quadre [ ] (Color Giallo/Arancio)
            Regex("""[{}\\]""").findAll(inputText).forEach { match ->
                addStyle(
                    style = SpanStyle(color = Color(0xFFFFC66D)),
                    start = match.range.first,
                    end = match.range.last + 1
                )
            }

            // 2. Comandi LaTeX es. \section, \textbf (Azzurro IDE)
            Regex("""\\[a-zA-Z]+""").findAll(inputText).forEach { match ->
                addStyle(
                    style = SpanStyle(color = Color(0xFF569CD6)),
                    start = match.range.first,
                    end = match.range.last + 1
                )
            }

            // 3. Formule Matematiche $...$ (Viola/Rosa)
            Regex("""\$.*?\$""").findAll(inputText).forEach { match ->
                addStyle(
                    style = SpanStyle(color = Color(0xFFC586C0)),
                    start = match.range.first,
                    end = match.range.last + 1
                )
            }

            // 4. Commenti % (Verde) - Lo mettiamo per ultimo così sovrascrive il colore dei comandi se commentati!
            Regex("""%.*""").findAll(inputText).forEach { match ->
                addStyle(
                    style = SpanStyle(color = Color(0xFF6A9955)),
                    start = match.range.first,
                    end = match.range.last + 1
                )
            }
        }
        return TransformedText(annotatedString, OffsetMapping.Identity)
    }
}