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
fun NewNotePage(
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
                text = "Create A New Note",
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

            Text(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                text = "Type of Note",
                color = colorPalette.primary,
                textAlign = TextAlign.Start,
                fontSize = MaterialTheme.typography.displaySmall.fontSize,
            )

            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(20.dp),
                text = "Type: ${typeString.value}",
                color = colorPalette.primary,
                textAlign = TextAlign.Start,
                fontSize = MaterialTheme.typography.displaySmall.fontSize,
            )

            DropDownMenu(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                typeString,
                listOf("University", "Work", "Other")
            )

            ButtonCard(
                Modifier.padding(0.dp, 40.dp, 0.dp, 40.dp).fillMaxWidth(),
                "CREATE NOTE",
                {
                    runBlocking {
                        ServerRequests.createNote(
                            CreateNoteJson(
                                titleString.value,
                                typeString.value,
                                0,
                                1,
                                settingsRepository.getSavePathFlow("/home/USER/documents/") + titleString.value.trim() + ".txt"
                            )
                        )
                    }
                    fileSaver.saveNoteToDisk("", settingsRepository.getSavePathFlow("/home/USER/documents/") + titleString.value.trim() + ".txt")
                    navigator.navigate("notes")
                },
                colorPalette
            )
        }
    }
}

@Composable
fun DropDownMenu(
    modifier: Modifier = Modifier,
    typeString: MutableState<String>,
    menuItemData: List<String>
) {
    var expanded = remember { mutableStateOf(false) }
    // Placeholder list of 100 strings for demonstration


    Box(
        modifier = modifier
    ) {
        Button(onClick = { expanded.value = true }) { Text("Select Type of the new Note") }
        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            menuItemData.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        typeString.value = option
                        expanded.value = false
                    }
                )
            }
        }
    }
}