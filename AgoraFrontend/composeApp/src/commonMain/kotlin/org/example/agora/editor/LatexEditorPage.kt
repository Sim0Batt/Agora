package org.example.agora.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
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
import org.example.agora.utils.FileSaver
import org.example.agora.utils.LatexCodeConverter
import org.example.agora.utils.SettingsRepository

@Composable
fun LatexEditorPage(
    colorPalette: ColorScheme,
    modifier: Modifier = Modifier,
    settingsRepository: SettingsRepository,
    fileSaver: FileSaver,
    documentTitle: String = "Documento",
    latexCodeConverter: LatexCodeConverter
) {
    // 1. Usiamo TextFieldValue invece di String per controllare il cursore e la selezione!
    var content by remember { mutableStateOf(TextFieldValue("")) }

    // 2. CoroutineScope per non bloccare l'interfaccia durante la generazione
    val scope = rememberCoroutineScope()
    var isCompiling by remember { mutableStateOf(false) }

    // --- FUNZIONE HELPER PER LA TOOLBAR ---
    // Avvolge il testo selezionato con i comandi LaTeX (es. \textbf{...})
    fun insertLatexCommand(prefix: String, suffix: String = "}") {
        val currentText = content.text
        val selection = content.selection
        val selectedText = currentText.substring(selection.start, selection.end)

        val newText = currentText.replaceRange(selection.start, selection.end, "$prefix$selectedText$suffix")
        // Riposiziona il cursore esattamente in mezzo alle graffe se non c'era testo selezionato
        val newCursorPos = selection.start + prefix.length + selectedText.length

        content = TextFieldValue(text = newText, selection = TextRange(newCursorPos))
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- HEADER: Titolo e Bottoni di Azione ---
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

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = colorPalette.primary),
                    enabled = !isCompiling,
                    onClick = {
                        scope.launch {
                            isCompiling = true
                            // Passiamo solo la stringa (content.text) al convertitore
                            latexCodeConverter.codeConverter(documentTitle, content.text)
                            isCompiling = false
                        }
                    }
                ) {
                    Text(if (isCompiling) "Generazione..." else "Genera PDF")
                }
            }
        }

        // --- TOOLBAR (Comandi Rapidi) ---
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

                // Grassetto
                IconButton(onClick = { insertLatexCommand("\\textbf{") }) {
                    Icon(Icons.Default.FormatBold, contentDescription = "Bold")
                }
                // Corsivo
                IconButton(onClick = { insertLatexCommand("\\textit{") }) {
                    Icon(Icons.Default.FormatItalic, contentDescription = "Italic")
                }
                // Elenco puntato
                IconButton(onClick = {
                    insertLatexCommand("\\begin{itemize}\n\\item ", "\n\\end{itemize}")
                }) {
                    Icon(Icons.Default.FormatListBulleted, contentDescription = "List")
                }
                // Sezione
                Button(
                    onClick = { insertLatexCommand("\\section{") },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("\\section")
                }
                // Equazione
                Button(
                    onClick = { insertLatexCommand("\\begin{equation}\n", "\n\\end{equation}") },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Math")
                }
            }
        }

        // --- SPLIT SCREEN (Editor + Preview) ---
        Row(
            modifier = Modifier.fillMaxSize().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // LATO SINISTRO: Editor di testo
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("Scrivi qui i tuoi appunti in LaTeX...") },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                // Usiamo un font Monospace (da programmatore) per leggere meglio il codice
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

            // LATO DESTRO: Anteprima PDF
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .background(Color.DarkGray.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                // TODO: Scommenta questo quando il fileSaver è pronto
                /*
                if(fileSaver.fileExists(settingsRepository.getPdfPath(""))){
                    PdfViewer("${settingsRepository.getPdfPath("")}/$documentTitle.pdf", modifier)
                } else {
                    Text("Nessuna anteprima disponibile.\nPremi 'Genera PDF'.", textAlign = TextAlign.Center)
                }
                */
                Text("Area Anteprima PDF", color = Color.Gray)
            }
        }
    }
}