package org.example.agora.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.runBlocking
import org.example.agora.ButtonCard
import org.example.agora.LocalNavigator
import org.example.agora.server.ServerRequests
import org.example.agora.server.models.CreateNoteJson
import org.example.agora.utils.FileSaver
import org.example.agora.utils.SettingsRepository


@Composable
fun NewLatexCreationPage(
    modifier: Modifier = Modifier,
    colorPalette: ColorScheme,
    settingsRepository: SettingsRepository,
    fileSaver: FileSaver
){
    val typeString = remember { mutableStateOf("") }
    val titleString = remember { mutableStateOf("") }
    val navigator = LocalNavigator.current


    Box(modifier = modifier){
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ){
            // Title
            Text(
                text = "Create A New Latex Note",
                color = colorPalette.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = MaterialTheme.typography.displayLarge.fontSize,
            )

            //Note Title
            Text(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                text = "Title",
                color = colorPalette.primary,
                textAlign = TextAlign.Start,
                fontSize = MaterialTheme.typography.displaySmall.fontSize,
            )
            OutlinedTextField(
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorPalette.primary,
                    unfocusedContainerColor = colorPalette.primary,
                ),
                value = titleString.value,
                onValueChange = {newText: String -> titleString.value = newText},
                label = { "Scrivi qualcosa qui..." },
                modifier = Modifier.fillMaxWidth()
            )

            ButtonCard(
                Modifier.padding(0.dp, 100.dp, 0.dp, 40.dp).fillMaxWidth(),
                "CREATE LATEX NOTE",
                {
                    runBlocking {
                        ServerRequests(settingsRepository).createLatexNote(titleString.value, 1)
                    }
                    navigator.navigate("latex")
                },
                colorPalette
            )
        }
    }
}
