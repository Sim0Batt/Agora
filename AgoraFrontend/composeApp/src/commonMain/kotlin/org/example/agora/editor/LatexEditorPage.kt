package org.example.agora.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.runBlocking
import org.example.agora.utils.FileSaver
import org.example.agora.utils.LatexCodeConverter
import org.example.agora.utils.SettingsRepository

@Composable
fun LatexEditorPage(
    colorPalette: ColorScheme,
    modifier: Modifier = Modifier,
    settingsRepository: SettingsRepository,
    fileSaver: FileSaver,
    documentTitle: String = "Documento",
    latexCodeConverter: LatexCodeConverter
) {
    var content by remember { mutableStateOf("") }

    // var controller by remember { Swing }



    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Latex Editor",
            style = MaterialTheme.typography.titleLarge,
            color = colorPalette.primary
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ){
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = colorPalette.primary),
                onClick = {
                    runBlocking {
                        latexCodeConverter.codeConverter(documentTitle, content)
                    }
                }
            ) {
                Text("Genera", color = colorPalette.secondary, fontSize = 16.sp)
            }
//
//            Button(
//                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
//                onClick = {
//                    runBlocking {
//                        ServerRequests.deleteNote(selectedNote.id)
//                        fileSaver.deleteFile(selectedNote.savedPath)
//                        navigator.navigate("home")
//                    }
//                }
//            ) {
//                Text("Elimina", color = colorPalette.primary, fontSize = 16.sp)
//            }


            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Codice LaTeX") },
                placeholder = { Text("Scrivi qui i tuoi appunti...") },
                modifier = Modifier
                    .fillMaxHeight()
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

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ){
                if(fileSaver.fileExists(settingsRepository.getPdfPath(""))){
                    PdfViewer("${settingsRepository.getPdfPath("")}/$documentTitle.pdf", modifier)
                }
            }
        }
    }
}