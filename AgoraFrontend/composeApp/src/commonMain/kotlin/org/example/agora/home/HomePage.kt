package org.example.agora.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.agora.server.ServerRequests
import org.example.agora.server.models.NotesJson
import androidx.compose.foundation.lazy.items

@Composable
fun HomePage(
    colorPalette: ColorScheme,
    modifier: Modifier = Modifier,
    editorCardOnClick: () -> Unit,
    newPageOnClick: () -> Unit,
    settingsOnClick: () -> Unit
){
    Column(modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(40.dp)
    ){
        var notesList = remember { mutableStateOf<List<NotesJson>>(emptyList()) }

        var isLoading = remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            try {
                val result = ServerRequests.getNotes("1")
                notesList = mutableStateOf(result)
            } catch (e: Exception) {
                println("Errore nel caricamento delle note: ${e.message}")
            } finally {
                isLoading = mutableStateOf(false)
            }
        }
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
                title = "Open Editors",
                text = "Click here to open the notes editor",
                onClick = editorCardOnClick,
                colorPalette = colorPalette
            )

            ActionCard(
                title = "Create a New Note",
                text = "Click here to create a new note",
                onClick = newPageOnClick,
                colorPalette = colorPalette
            )

            ActionCard(
                title = "Modify Settings",
                text = "Click here to modify the application settings",
                onClick = settingsOnClick,
                colorPalette = colorPalette
            )
        }

        Text(
            text = "Recently Opened Files",
            color = colorPalette.primary,
            textAlign = TextAlign.Start,
            fontSize = MaterialTheme.typography.displaySmall.fontSize,
        )
        if (isLoading.value) {
            // Mostra un caricamento mentre aspetti il server
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colorPalette.primary)
            }
        } else {
            LazyRow (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(25.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                println("Arrived NOtes")
                items(notesList.value) { note ->
                    NoteCard(
                        modifier = Modifier,
                        title = note.title,
                        savedPath = note.savedPath,
                        colorPalette = colorPalette,
                        length = note.length,
                        onClick = {
                            println("Note Clicked")
                        }
                    )
                }
            }
        }
    }
}