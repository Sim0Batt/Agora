package org.example.agora.notesCopy

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.runBlocking
import org.example.agora.editor.LatexIDE
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
    fileSaver: FileSaver
) {
    var latexFiles by remember { mutableStateOf(listOf<String>()) }
    var selectedLatexFile by remember { mutableStateOf("") }
    var latexMenuExpanded by remember { mutableStateOf(false) }

    var normalNotes by remember { mutableStateOf(listOf<String>()) }
    var selectedNormalNote by remember { mutableStateOf("") }
    var normalMenuExpanded by remember { mutableStateOf(false) }

    var selectedNoteContent by remember { mutableStateOf("") }
    var selectedLatexNoteContent by remember { mutableStateOf("") }

    var currentLatexNoteId by remember { mutableStateOf(-1) }
    var currentNormalNoteId by remember { mutableStateOf(-1) }



    LaunchedEffect(settingsRepository) {
        latexFiles = ServerRequests(settingsRepository).getLatexNotesList(1).map { "${it.id} - ${it.title}" }

        normalNotes = ServerRequests(settingsRepository).getNotes("1", fileSaver).map { "${it.id} - ${it.title}" }

        println(latexFiles)
        println(normalNotes)

        currentLatexNoteId = if(settingsRepository.getLastLatexId() != -1){
            settingsRepository.getLastLatexId()
        }else if(latexFiles.isNotEmpty()){
            latexFiles.first().split(" - ")[0].toInt()
        }else{
            -1
        }

        currentNormalNoteId = if(normalNotes.isNotEmpty()){
            normalNotes.first().split(" - ")[0].toInt()
        }else{
            -1
        }

    }


    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Copy Notes in LaTeX",
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
                                    selectedLatexNoteContent = runBlocking{
                                        ServerRequests(settingsRepository).getLatexContent(file.split(" - ")[0].toInt())
                                    }

                                }
                            )
                        }
                    }
                }

                LatexIDE(colorPalette, modifier, selectedLatexNoteContent, settingsRepository, currentLatexNoteId)
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
                                    selectedNoteContent = runBlocking { ServerRequests(settingsRepository).getNotesContent(currentNormalNoteId) }
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = selectedNoteContent,
                    onValueChange = {},
                    readOnly = true,
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