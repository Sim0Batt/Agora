package org.example.agora.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.agora.home.NoteCard
import org.example.agora.server.ServerRequests
import org.example.agora.server.models.NotesJson
import org.example.agora.settings.FileSaver

@Composable
fun NotesPage (
    modifier: Modifier = Modifier,
    colorPalette: ColorScheme,
    fileSaver: FileSaver
){
    var notesList by remember { mutableStateOf<List<NotesJson>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            notesList = ServerRequests.getNotes("1", fileSaver)
        } catch (e: Exception) {
            println("Errore nel caricamento delle note: ${e.message}")
        }
    }
    Column(modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(40.dp)
    ){
        Text(
            text = "All Notes",
            color = colorPalette.primary,
            textAlign = TextAlign.Start,
            fontSize = MaterialTheme.typography.displaySmall.fontSize,
        )
        if (notesList.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(25.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(notesList) { note ->
                    NoteCard(
                        modifier = Modifier,
                        title = note.title,
                        savedPath = note.savedPath,
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