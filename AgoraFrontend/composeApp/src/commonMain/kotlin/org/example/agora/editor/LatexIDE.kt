package org.example.agora.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material3.Button
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.agora.utils.LATEX_COMMANDS
import org.example.agora.utils.LatexSyntaxTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LatexIDE(
    colorPalette: ColorScheme,
    modifier: Modifier = Modifier,

) {
    Surface(
    modifier = Modifier.fillMaxWidth(),
    color = colorPalette.surfaceVariant.copy(alpha = 0.5f),
    shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Insert:", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(end = 8.dp))

            IconButton(onClick = { insertLatexCommand("\\textbf{") }) {
                Icon(Icons.Default.FormatBold, contentDescription = "Grassetto")
            }
            IconButton(onClick = { insertLatexCommand("\\textit{") }) {
                Icon(Icons.Default.FormatItalic, contentDescription = "Corsivo")
            }
            IconButton(onClick = {
                insertLatexCommand("\\begin{itemize}\n\\item ", "\n\\end{itemize}")
            }) {
                Icon(Icons.Default.FormatListBulleted, contentDescription = "Elenco Puntato")
            }
            Button(
                onClick = { insertLatexCommand("\\section{") },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("\\section")
            }
            Button(
                onClick = { insertLatexCommand("\\begin{equation}\n", "\n\\end{equation}") },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Math")
            }
        }
    }


    // --- SPLIT SCREEN: EDITOR A SINISTRA, SUGGERIMENTI A DESTRA ---
    Row(
    modifier = Modifier.fillMaxWidth().weight(1f),
    horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. EDITOR PRINCIPALE (Si allarga per prendere lo spazio) ---
        OutlinedTextField(
            value = content,
            onValueChange = { newValue ->
                content = newValue

                val cursor = newValue.selection.start
                val textBeforeCursor = newValue.text.substring(0, cursor)
                val lastWord = textBeforeCursor.split(Regex("\\s+")).lastOrNull() ?: ""

                if (lastWord.startsWith("\\") && lastWord.length > 1) {
                    currentWord = lastWord.replace("\\", "")
                    suggestedCommands = LATEX_COMMANDS.filter { it.contains(currentWord) }
                } else {
                    suggestedCommands = emptyList()
                }
            },
            placeholder = { Text("Scrivi qui i tuoi appunti in LaTeX...") },

            visualTransformation = LatexSyntaxTransformation(
                commandColor = colorPalette.primary,
                bracketColor = colorPalette.tertiary
            ),

            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {

                        val isConfirmKey = event.key == Key.Tab || event.key == Key.Enter || event.key == Key.NumPadEnter
                        if (isConfirmKey && suggestedCommands.isNotEmpty()) {
                            applySuggestion(suggestedCommands.first())
                            return@onPreviewKeyEvent true
                        }

                        if (event.key == Key.Escape && suggestedCommands.isNotEmpty()) {
                            suggestedCommands = emptyList()
                            return@onPreviewKeyEvent true
                        }

                        val isCtrlOrCmd = event.isCtrlPressed || event.isMetaPressed
                        if (isCtrlOrCmd) {
                            when (event.key) {
                                Key.B -> {
                                    insertLatexCommand("\\textbf{")
                                    return@onPreviewKeyEvent true
                                }
                                Key.I -> {
                                    insertLatexCommand("\\textit{")
                                    return@onPreviewKeyEvent true
                                }
                                else -> return@onPreviewKeyEvent false
                            }
                        }
                    }
                    false
                },
            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = colorPalette.onSurface),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorPalette.primary,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
            )
        )

        // --- 2. PANNELLO LATERALE AUTOCOMPLETAMENTO ---
        AnimatedVisibility(visible = suggestedCommands.isNotEmpty()) {
            Surface(
                modifier = Modifier.width(250.dp).fillMaxHeight(),
                color = colorPalette.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Suggestions (Press Tab)",
                        style = MaterialTheme.typography.labelMedium,
                        color = colorPalette.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyColumn (
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(suggestedCommands) { suggestion ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.Transparent,
                                shape = RoundedCornerShape(4.dp),
                                onClick = { applySuggestion(suggestion) }
                            ) {
                                Text(
                                    text = suggestion,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(8.dp),
                                    color = colorPalette.primary
                                )
                            }
                        }
                    }
                }
            }
        }
}