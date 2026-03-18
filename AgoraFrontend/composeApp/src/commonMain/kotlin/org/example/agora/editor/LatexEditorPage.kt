package org.example.agora.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.util.debug.plugins.PluginName
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.example.agora.LocalNavigator
import org.example.agora.server.ServerRequests
import org.example.agora.utils.FileSaver
import org.example.agora.utils.LATEX_COMMANDS
import org.example.agora.utils.LatexCodeConverter
import org.example.agora.utils.LatexSyntaxTransformation
import org.example.agora.utils.SettingsRepository



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LatexEditorPage(
    colorPalette: ColorScheme,
    modifier: Modifier = Modifier,
    settingsRepository: SettingsRepository,
    fileSaver: FileSaver,
    latexCodeConverter: LatexCodeConverter
) {
    var content by remember { mutableStateOf(TextFieldValue("")) }
    val scope = rememberCoroutineScope()
    var isCompiling by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var itemsMenu by remember { mutableStateOf(listOf<String>()) }
    var selectedOption by remember { mutableStateOf(if(itemsMenu.isNotEmpty()) itemsMenu.first() else "") }
    var currentWord by remember { mutableStateOf("") }
    var suggestedCommands by remember { mutableStateOf<List<String>>(emptyList()) }
    var documentTitle by remember { mutableStateOf("Document") }

    var currentNoteId by remember { mutableStateOf(-1) }

    val navigator = LocalNavigator.current


    LaunchedEffect(settingsRepository) {
        val notes = ServerRequests(settingsRepository).getLatexNotesList(1)
        itemsMenu = notes.map {
            "${it.id} - ${it.title}"
        }
        if (notes.isNotEmpty()) {
            if(settingsRepository.getLastLatexId() == -1) {
                settingsRepository.saveLastLatexId(notes.first().id)
                selectedOption = "${notes.first().id} - ${notes.first().title}"
                documentTitle = notes.first().title
            }else{
                if(settingsRepository.getLastLatexId() in notes.map { it.id }) {
                    selectedOption = itemsMenu.first { it.split(" - ")[0].toInt() == settingsRepository.getLastLatexId() }
                    documentTitle = selectedOption.split(" - ")[1]
                }else{
                    settingsRepository.saveLastLatexId(notes.first().id)
                    selectedOption = "${notes.first().id} - ${notes.first().title}"
                    documentTitle = notes.first().title
                }
            }
        }

        currentNoteId = if(settingsRepository.getLastLatexId() == -1 && notes.isNotEmpty()) notes.first().id else settingsRepository.getLastLatexId()

        content = TextFieldValue(ServerRequests(settingsRepository).getLatexContent(currentNoteId))
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LaTeX Editor - $documentTitle",
                style = MaterialTheme.typography.headlineMedium,
                color = colorPalette.primary
            )

            Button(
                colors = ButtonDefaults.buttonColors(containerColor = colorPalette.secondary),
                onClick = {
                    scope.launch {
                        navigator.navigate("newLatexNote")
                    }
                }
            ) {
                Text("Create New Latex Note")
            }

            Button(
                colors = ButtonDefaults.buttonColors(containerColor = colorPalette.secondary),
                enabled = !isCompiling,
                onClick = {
                    scope.launch {
                        isCompiling = true
                        val contentSaved = ServerRequests(settingsRepository).getLatexContent(currentNoteId)
                        latexCodeConverter.codeConverter(documentTitle, contentSaved)
                        isCompiling = false
                    }
                }
            ) {
                Text(if (isCompiling) "Generating..." else "Generate PDF")
            }

            Button(
                colors = ButtonDefaults.buttonColors(containerColor = colorPalette.secondary),
                enabled = !isCompiling,
                onClick = {
                    scope.launch {
                        ServerRequests(settingsRepository).deleteLatexNote(currentNoteId)
                        navigator.navigate("home")
                    }
                }
            ) {
                Text("Delete Note")
            }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedOption,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.menuAnchor(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    itemsMenu.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                selectedOption = selectionOption
                                content = runBlocking {
                                    TextFieldValue(ServerRequests(settingsRepository).getLatexContent(
                                        selectionOption.split(" - ")[0].toInt()
                                        )
                                    )

                                }
                                currentNoteId = selectionOption.split(" - ")[0].toInt()

                                settingsRepository.saveLastLatexId(currentNoteId)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        LatexIDE(colorPalette, modifier, content.text, settingsRepository, currentNoteId)
    }
}