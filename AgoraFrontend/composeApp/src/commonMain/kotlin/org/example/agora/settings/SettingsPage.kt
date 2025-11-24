package org.example.agora.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.agora.ButtonCard
import org.example.agora.editor.DropDownMenu

@Composable
fun SettingsPage (
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier,
    colorPalette: ColorScheme
){
    Box(modifier = modifier){
        val pathString = remember { mutableStateOf("") }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ){
            // Title
            Text(
                text = "Personalize the Settings",
                color = colorPalette.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = MaterialTheme.typography.displayLarge.fontSize,
            )

            //Path selector
            Text(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                text = "Saving Path",
                color = colorPalette.primary,
                textAlign = TextAlign.Start,
                fontSize = MaterialTheme.typography.displaySmall.fontSize,
            )

            OutlinedTextField(
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorPalette.primary,
                    unfocusedContainerColor = colorPalette.primary,
                ),
                value = pathString.value,
                onValueChange = {newPath: String ->
                    run {
                        pathString.value = newPath
                    }
                },
                label = { Text(settingsRepository.getSavePathFlow("/home/USER/documentis/")) },
                modifier = Modifier.fillMaxWidth()
            )
            ButtonCard(
                Modifier.padding(0.dp, 40.dp, 0.dp, 40.dp).fillMaxWidth(),
                "Save Settings",
                {
                    settingsRepository.setSavePath(pathString.value)
                    println("Saved path: ${pathString.value}")
                },
                colorPalette
            )
        }
    }

}