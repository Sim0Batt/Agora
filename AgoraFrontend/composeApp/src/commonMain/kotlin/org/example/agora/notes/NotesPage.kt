package org.example.agora.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import org.example.agora.drawable.NoteCard
import org.example.agora.server.ServerRequests
import org.example.agora.server.models.NotesJson
import org.example.agora.utils.FileSaver
import org.example.agora.utils.SettingsRepository

@Composable
fun NotesPage (
    modifier: Modifier = Modifier,
    colorPalette: ColorScheme,
    fileSaver: FileSaver,
    settingsRepository: SettingsRepository
){
    var notesList by remember { mutableStateOf<List<NotesJson>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            notesList = ServerRequests(settingsRepository).getNotes("1", fileSaver)
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
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),

                contentPadding = PaddingValues(16.dp)
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