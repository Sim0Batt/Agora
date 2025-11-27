package org.example.agora.converter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.agora.LatexConverter

@Composable
fun ConverterPage(
    colorPalette: ColorScheme,
    modifier: Modifier = Modifier
) {
    var inputString by remember { mutableStateOf("") }
    var outputString by remember { mutableStateOf("") }

    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Convertitore LaTeX",
            style = MaterialTheme.typography.headlineMedium,
            color = colorPalette.primary,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // Occupa metà dello spazio disponibile
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, colorPalette.outline.copy(alpha = 0.5f))
        ) {
            Column(Modifier.fillMaxSize().padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Testo Grezzo", style = MaterialTheme.typography.titleSmall, color = colorPalette.primary)
                }

                OutlinedTextField(
                    value = inputString,
                    onValueChange = { inputString = it },
                    modifier = Modifier.fillMaxSize(),
                    placeholder = { Text("Incolla qui il tuo testo...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        focusedLabelColor = Color.White,
                        cursorColor = Color.White,
                        focusedContainerColor = colorPalette.secondary,
                        unfocusedContainerColor = colorPalette.secondary
                    ),
                    textStyle = TextStyle(fontSize = 16.sp)
                )
            }
        }

        Button(
            onClick = {
                if (inputString.isNotEmpty()) {
                    outputString = LatexConverter.convert(inputString)
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally).height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorPalette.primary),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Converti in LaTeX", fontSize = 18.sp, color = Color.Black)
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // Occupa l'altra metà
            colors = CardDefaults.cardColors(containerColor = colorPalette.surfaceVariant.copy(alpha = 0.3f)),
            border = BorderStroke(1.dp, colorPalette.outline.copy(alpha = 0.5f))
        ) {
            Column(Modifier.fillMaxSize().padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Codice Generato", style = MaterialTheme.typography.titleSmall, color = colorPalette.primary)

                    // Bottone Copia
                    FilledTonalButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(outputString))
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = colorPalette.secondaryContainer)
                    ) {
                        Text("Copia")
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Area di testo sola lettura con font monospaziato
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorPalette.surface, RoundedCornerShape(4.dp))
                        .border(1.dp, colorPalette.outline.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    val scrollState = rememberScrollState()

                    Text(
                        text = outputString.ifEmpty { "Il codice comparirà qui..." },
                        color = if (outputString.isNotEmpty()) colorPalette.onSurface else colorPalette.onSurface.copy(alpha = 0.5f),
                        fontFamily = FontFamily.Monospace, // Font da codice
                        style = TextStyle(lineHeight = 20.sp),
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState) // Abilita lo scroll
                    )
                }
            }
        }
    }
}