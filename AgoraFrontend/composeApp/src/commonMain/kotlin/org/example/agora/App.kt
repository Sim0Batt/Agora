package org.example.agora

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.agora.editor.EditorPage
import org.example.agora.editor.NewNotePage
import org.example.agora.home.HomePage
import org.example.agora.settings.SettingsPage


private val LightColorPalette = lightColorScheme(
    primary = Color(0xFF6200EE),
    secondary = Color(0xFF03DAC5),
    background = Color(234,224,200),
)

private val DarkColorPalette = darkColorScheme(
    primary = Color(255, 255, 255, 255),
    secondary = Color(0xFF5C6362),
    background = Color(56, 53, 53, 255),
)

private val SemiDarkColorPalette = darkColorScheme(
    primary = Color.White,
    secondary = Color(0xFF03DAC5),
    background = Color(43, 43, 41),
)

@Composable
@Preview
fun App(
    modifier: Modifier = Modifier
) {
    var selectedRoute by remember { mutableStateOf("home") }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = DarkColorPalette.background
    ) {
        Row (Modifier.fillMaxSize()){
            NavBar(
                selectedRoute = selectedRoute,
                onRouteSelected = { route ->
                    selectedRoute = route
                },
                modifier = Modifier.width(240.dp),
                SemiDarkColorPalette
            )

            Box(
                modifier = Modifier.weight(1f).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                when (selectedRoute) {
                    "home" -> HomePage(
                        DarkColorPalette,
                        modifier = Modifier.fillMaxHeight(),
                        editorCardOnClick = { selectedRoute = "editor" },
                        newPageOnClick = { selectedRoute = "new_page" }
                    )
                    "editor" -> EditorPage(modifier ,DarkColorPalette)
                    "settings" -> SettingsPage(modifier, DarkColorPalette)
                    "new_page" -> NewNotePage(modifier, DarkColorPalette)
                }
            }
        }
    }
}