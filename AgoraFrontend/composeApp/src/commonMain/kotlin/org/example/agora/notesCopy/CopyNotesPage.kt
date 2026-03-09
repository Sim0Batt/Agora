package org.example.agora.notesCopy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.agora.editor.LatexEditorPage
import org.example.agora.server.ServerRequests
import org.example.agora.utils.FileSaver
import org.example.agora.utils.LatexCodeConverter
import org.example.agora.utils.SettingsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CopyNotesPage(
    colorPalette: ColorScheme,
    modifier: Modifier = Modifier,
    settingsRepository: SettingsRepository,
    latexCodeConverter: LatexCodeConverter,
    fileSaver: FileSaver
) {
    var latexFiles by remember { mutableStateOf(listOf<String>()) }
    var selectedLatexFile by remember { mutableStateOf("") }
    var latexMenuExpanded by remember { mutableStateOf(false) }

    val normalNotes = listOf("Lezione 1", "Lezione 2", "Appunti Sparsi")
    var selectedNormalNote by remember { mutableStateOf(normalNotes[0]) }
    var normalMenuExpanded by remember { mutableStateOf(false) }

    var normalNoteContent by remember { mutableStateOf("") }


    LaunchedEffect(settingsRepository) {
        latexFiles = ServerRequests(settingsRepository).getLatexNotesList(1).map { "${it.id} - ${it.title}" }
    }


    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Trascrizione in LaTeX",
            style = MaterialTheme.typography.headlineMedium,
            color = colorPalette.primary
        )

        // --- SPLIT SCREEN PRINCIPALE ---
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==========================================
            // LATO SINISTRO: EDITOR LATEX (MODIFICABILE)
            // ==========================================
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Selettore File LaTeX
                ExposedDropdownMenuBox(
                    expanded = latexMenuExpanded,
                    onExpandedChange = { latexMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedLatexFile,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("File LaTeX Destinazione") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = latexMenuExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedBorderColor = colorPalette.primary,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = latexMenuExpanded,
                        onDismissRequest = { latexMenuExpanded = false }
                    ) {
                        latexFiles.forEach { file ->
                            DropdownMenuItem(
                                text = { Text(file) },
                                onClick = {
                                    selectedLatexFile = file
                                    latexMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(1.dp, colorPalette.primary, RoundedCornerShape(8.dp))
                        .background(colorPalette.surfaceVariant.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    LatexEditorPage(colorPalette, modifier, settingsRepository, fileSaver, latexCodeConverter)
                }
            }



            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Selettore Appunti Normali
                ExposedDropdownMenuBox(
                    expanded = normalMenuExpanded,
                    onExpandedChange = { normalMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedNormalNote,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Appunti da Trascrivere") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = normalMenuExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedBorderColor = colorPalette.secondary,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = normalMenuExpanded,
                        onDismissRequest = { normalMenuExpanded = false }
                    ) {
                        normalNotes.forEach { note ->
                            DropdownMenuItem(
                                text = { Text(note) },
                                onClick = {
                                    selectedNormalNote = note
                                    normalMenuExpanded = false
                                    // TODO: Qui dovrai caricare il contenuto della nota selezionata
                                    // normalNoteContent = fetchNoteContent(note.id)
                                }
                            )
                        }
                    }
                }

                // VISUALIZZATORE APPUNTI (Read Only)
                OutlinedTextField(
                    value = normalNoteContent,
                    onValueChange = {}, // Ignora qualsiasi input
                    readOnly = true,    // Impedisce la modifica e l'apertura della tastiera su mobile
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = colorPalette.onSurface
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = colorPalette.surfaceVariant.copy(alpha = 0.2f),
                        unfocusedContainerColor = colorPalette.surfaceVariant.copy(alpha = 0.2f)
                    )
                )
            }
        }
    }
}