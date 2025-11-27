package org.example.agora.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.runBlocking
import org.example.agora.LatexConverter
import org.example.agora.server.ServerRequests
import org.example.agora.server.models.CreateNoteJson
import org.example.agora.server.models.NotesJson
import org.example.agora.settings.FileSaver
import org.example.agora.settings.SettingsRepository

@Composable
fun NotesEditor(
    colorPalette: ColorScheme,
    modifier: Modifier = Modifier,
    settingsRepository: SettingsRepository,
    noteId: Int = 0,
    fileSaver: FileSaver
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var generatedLatex by remember { mutableStateOf("") }

    var showLatexDialog by remember { mutableStateOf(false) }

    var notesList by remember { mutableStateOf<List<NotesJson>>(emptyList()) }
    var selectedNote by remember { mutableStateOf(NotesJson(-1, "", "", 0, "", -1)) }

    LaunchedEffect(Unit) {
        try {
            notesList = ServerRequests.getNotes("1").reversed().take(3)
            notesList.map {
                if (it.id == noteId) selectedNote = it
            }
            title = selectedNote.title
            content = fileSaver.getFileText(selectedNote.savedPath)
        } catch (e: Exception) {
            println("Errore nel caricamento delle note: ${e.message}")
        }
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Editor Note",
                style = MaterialTheme.typography.titleLarge,
                color = colorPalette.primary
            )

            Button(
                colors = ButtonDefaults.buttonColors(containerColor = colorPalette.primary),
                onClick = {
                    runBlocking {
//                        ServerRequests.deleteNote(selectedNote.id)
//                        ServerRequests.createNote(
//                            CreateNoteJson(
//                                title,
//                                selectedNote.type,
//                                content.length,
//                                selectedNote.userId,
//                                selectedNote.savedPath
//                            )
//                        )
                        fileSaver.saveNoteToDisk(content, selectedNote.savedPath)
                    }
                }
            ) {
                Text("Salva", color = colorPalette.secondary, fontSize = 16.sp)
            }

            OutlinedButton(
                onClick = {
                    generatedLatex = LatexConverter.convert(content)
                    showLatexDialog = true
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colorPalette.primary)
            ) {
                Text("Esporta Latex", color = colorPalette.secondary, fontSize = 16.sp)
            }
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Titolo") },
            placeholder = { Text("Inserisci il titolo...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White,
                focusedLabelColor = Color.White,
                cursorColor = Color.White,
                focusedContainerColor = colorPalette.secondary,
                unfocusedContainerColor = colorPalette.secondary
            )
        )

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Contenuto") },
            placeholder = { Text("Scrivi qui i tuoi appunti...") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            textStyle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White,
                focusedLabelColor = Color.White,
                cursorColor = Color.White,
                focusedContainerColor = colorPalette.secondary,
                unfocusedContainerColor = colorPalette.secondary
            )
        )

        if (showLatexDialog) {
            LatexPreviewDialog(
                latexCode = generatedLatex,
                onDismiss = { showLatexDialog = false }
            )
        }
    }
}