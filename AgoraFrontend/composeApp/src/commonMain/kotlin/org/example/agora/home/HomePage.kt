package org.example.agora.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.agora.editor.NewNotePage


@Composable
fun HomePage(
    colorPalette: ColorScheme,
    modifier: Modifier = Modifier,
    editorCardOnClick: () -> Unit,
    newPageOnClick: () -> Unit
){
    Column(modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(40.dp)
    ){
        Text(
            text = "Commons",
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
        }

        Text(
            text = "Recently Opened Files",
            color = colorPalette.primary,
            textAlign = TextAlign.Start,
            fontSize = MaterialTheme.typography.displaySmall.fontSize,
        )
        Row (
            modifier = Modifier.fillMaxWidth(),
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
        }
    }
}