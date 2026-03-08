package org.example.agora.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.agora.settings.UtilsFunctions
import org.example.agora.utils.FileSaver
import org.example.agora.utils.LatexCodeConverter
import org.example.agora.utils.SettingsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LatexEditorPage(
    colorPalette: ColorScheme,
    modifier: Modifier = Modifier,
    settingsRepository: SettingsRepository,
    fileSaver: FileSaver,
    documentTitle: String = "Documento",
    latexCodeConverter: LatexCodeConverter
) {
    var content by remember { mutableStateOf(TextFieldValue("")) }
    val scope = rememberCoroutineScope()
    var isCompiling by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var itemsMenu by remember { mutableStateOf(listOf<String>("asdasa", "sadasdas", "asdasdas")) }

    fun insertLatexCommand(prefix: String, suffix: String = "}") {
        val currentText = content.text
        val selection = content.selection
        val selectedText = currentText.substring(selection.start, selection.end)

        val newText = currentText.replaceRange(selection.start, selection.end, "$prefix$selectedText$suffix")
        val newCursorPos = selection.start + prefix.length + selectedText.length

        content = TextFieldValue(text = newText, selection = TextRange(newCursorPos))
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Editor LaTeX - $documentTitle",
                style = MaterialTheme.typography.headlineMedium,
                color = colorPalette.primary
            )

            Button(
                colors = ButtonDefaults.buttonColors(containerColor = colorPalette.secondary),
                enabled = !isCompiling,
                onClick = {
                    scope.launch {
                        isCompiling = true
                        latexCodeConverter.codeConverter(documentTitle, content.text)
                        isCompiling = false
                    }
                }
            ) {
                Text(if (isCompiling) "Generating..." else "Generate PDF")
            }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = false }
            ) {
                // Genera un elemento cliccabile per ogni opzione nella lista
                itemsMenu.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            UtilsFunctions.onOptionSelected("") // Passa la scelta al genitore
                            expanded = false                  // Chiudi la tendina
                        }
                    )
                }
            }
        }

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

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            placeholder = { Text("Scrivi qui i tuoi appunti in LaTeX...") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = colorPalette.onSurface
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorPalette.primary,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                focusedContainerColor = colorPalette.surface,
                unfocusedContainerColor = colorPalette.surface
            )
        )
    }


}