package org.example.agora.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.agora.server.ServerRequests
import org.example.agora.server.models.NotesJson
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.example.agora.LocalNavigator
import org.example.agora.drawable.ActionCard
import org.example.agora.drawable.NoteCard
import org.example.agora.utils.FileSaver
import org.example.agora.utils.SettingsRepository

@Composable
fun HomePage(
    colorPalette: ColorScheme,
    modifier: Modifier = Modifier,
    fileSaver: FileSaver,
    settingsRepository: SettingsRepository
){
    var notesList by remember { mutableStateOf<List<NotesJson>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            notesList = ServerRequests(settingsRepository).getNotes("1", fileSaver).reversed().take(3)
        } catch (e: Exception) {
            println("Errore nel caricamento delle note: ${e.message}")
        }
    }

    val navigator = LocalNavigator.current

    Column(modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(40.dp)
    ){
        Text(
            text = "Common Operations",
            color = colorPalette.primary,
            textAlign = TextAlign.Start,
            fontSize = MaterialTheme.typography.displaySmall.fontSize,
        )
        Row (
            modifier = Modifier.fillMaxWidth().padding(0.dp, 0.dp, 0.dp, 40.dp),
            horizontalArrangement = Arrangement.spacedBy(25.dp),
        ){
             ActionCard(
                 title = "Open Notes",
                 text = "Click here to open the notes editor",
                 onClick = {
                     navigator.navigate("notes")
                 },
                 colorPalette = colorPalette
             )

            ActionCard(
                title = "Create a New Note",
                text = "Click here to create a new note",
                onClick = {
                    navigator.navigate("new_page")
                },
                colorPalette = colorPalette
            )

            ActionCard(
                title = "Modify Settings",
                text = "Click here to modify the application settings",
                onClick = {
                    navigator.navigate("settings")
                },
                colorPalette = colorPalette
            )
        }

        Text(
            text = "Recently Opened Files",
            color = colorPalette.primary,
            textAlign = TextAlign.Start,
            fontSize = MaterialTheme.typography.displaySmall.fontSize,
        )

        if(notesList.isNotEmpty()){
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(25.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(notesList) { note ->
                    NoteCard(
                        modifier = Modifier,
                        title = note.title,
                        colorPalette = colorPalette,
                        length = note.length,
                        noteId = note.id,
                        type = note.type
                    )
                }
            }
        }

    }
}