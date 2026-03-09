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

        currentNoteId = if(settingsRepository.getLastLatexId() == -1) notes.first().id else settingsRepository.getLastLatexId()

        content = TextFieldValue(ServerRequests(settingsRepository).getLatexContent(currentNoteId))
    }

    fun insertLatexCommand(prefix: String, suffix: String = "}") {
        val currentText = content.text
        val selection = content.selection
        val selectedText = currentText.substring(selection.start, selection.end)

        val newText = currentText.replaceRange(selection.start, selection.end, "$prefix$selectedText$suffix")
        val newCursorPos = selection.start + prefix.length + selectedText.length

        content = TextFieldValue(text = newText, selection = TextRange(newCursorPos))
    }

    fun applySuggestion(suggestion: String) {
        val text = content.text
        val cursor = content.selection.start

        val startIndex = cursor - (currentWord.length + 1) // Considero la lunghezza della parola più la \

        var textToInsert = suggestion
        var cursorOffset = suggestion.length

        if (suggestion.startsWith("\\begin{")) {
            val envName = suggestion.substringAfter("\\begin{").substringBefore("}")
            val endStatement = "\\end{$envName}"

            textToInsert = "$suggestion\n    \n$endStatement"

            cursorOffset = suggestion.length + 5
        }
        else if (suggestion.endsWith("{}")) {
            cursorOffset = suggestion.length - 1
        }

        val newText = text.replaceRange(startIndex, cursor, textToInsert)

        val newCursorPos = startIndex + cursorOffset

        content = TextFieldValue(text = newText, selection = TextRange(newCursorPos))
        suggestedCommands = emptyList()
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
                        latexCodeConverter.codeConverter(documentTitle, content.text)
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
                        isCompiling = true
                        ServerRequests(settingsRepository).updateLatexNote(currentNoteId, content.text)
                        isCompiling = false
                    }
                }
            ) {
                Text("Save")
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

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colorPalette.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Insert:", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(end = 8.dp))

                IconButton(onClick = { insertLatexCommand("\\textbf{") }) {
                    Icon(Icons.Default.FormatBold, contentDescription = "Grassetto")
                }
                IconButton(onClick = { insertLatexCommand("\\textit{") }) {
                    Icon(Icons.Default.FormatItalic, contentDescription = "Corsivo")
                }
                IconButton(onClick = {
                    insertLatexCommand("\\begin{itemize}\n\\item ", "\n\\end{itemize}")
                }) {
                    Icon(Icons.Default.FormatListBulleted, contentDescription = "Elenco Puntato")
                }
                Button(
                    onClick = { insertLatexCommand("\\section{") },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("\\section")
                }
                Button(
                    onClick = { insertLatexCommand("\\begin{equation}\n", "\n\\end{equation}") },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Math")
                }
            }
        }


        // --- SPLIT SCREEN: EDITOR A SINISTRA, SUGGERIMENTI A DESTRA ---
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. EDITOR PRINCIPALE (Si allarga per prendere lo spazio) ---
            OutlinedTextField(
                value = content,
                onValueChange = { newValue ->
                    content = newValue

                    val cursor = newValue.selection.start
                    val textBeforeCursor = newValue.text.substring(0, cursor)
                    val lastWord = textBeforeCursor.split(Regex("\\s+")).lastOrNull() ?: ""

                    if (lastWord.startsWith("\\") && lastWord.length > 1) {
                        currentWord = lastWord.replace("\\", "")
                        suggestedCommands = LATEX_COMMANDS.filter { it.contains(currentWord) }
                    } else {
                        suggestedCommands = emptyList()
                    }
                },
                placeholder = { Text("Scrivi qui i tuoi appunti in LaTeX...") },

                visualTransformation = LatexSyntaxTransformation(
                    commandColor = colorPalette.primary,
                    bracketColor = colorPalette.tertiary
                ),

                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown) {

                            val isConfirmKey = event.key == Key.Tab || event.key == Key.Enter || event.key == Key.NumPadEnter
                            if (isConfirmKey && suggestedCommands.isNotEmpty()) {
                                applySuggestion(suggestedCommands.first())
                                return@onPreviewKeyEvent true
                            }

                            if (event.key == Key.Escape && suggestedCommands.isNotEmpty()) {
                                suggestedCommands = emptyList()
                                return@onPreviewKeyEvent true
                            }

                            val isCtrlOrCmd = event.isCtrlPressed || event.isMetaPressed
                            if (isCtrlOrCmd) {
                                when (event.key) {
                                    Key.B -> {
                                        insertLatexCommand("\\textbf{")
                                        return@onPreviewKeyEvent true
                                    }
                                    Key.I -> {
                                        insertLatexCommand("\\textit{")
                                        return@onPreviewKeyEvent true
                                    }
                                    else -> return@onPreviewKeyEvent false
                                }
                            }
                        }
                        false
                    },
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = colorPalette.onSurface),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorPalette.primary,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                )
            )

            // --- 2. PANNELLO LATERALE AUTOCOMPLETAMENTO ---
            AnimatedVisibility(visible = suggestedCommands.isNotEmpty()) {
                Surface(
                    modifier = Modifier.width(250.dp).fillMaxHeight(),
                    color = colorPalette.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Suggestions (Press Tab)",
                            style = MaterialTheme.typography.labelMedium,
                            color = colorPalette.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        LazyColumn (
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(suggestedCommands) { suggestion ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color.Transparent,
                                    shape = RoundedCornerShape(4.dp),
                                    onClick = { applySuggestion(suggestion) }
                                ) {
                                    Text(
                                        text = suggestion,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(8.dp),
                                        color = colorPalette.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}