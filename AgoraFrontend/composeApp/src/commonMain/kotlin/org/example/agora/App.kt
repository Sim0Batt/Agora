package org.example.agora

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.agora.notesCopy.CopyNotesPage
import org.example.agora.converter.ConverterPage
import org.example.agora.editor.LatexEditorPage
import org.example.agora.notes.NotesPage
import org.example.agora.notes.NewNotePage
import org.example.agora.editor.NotesEditor
import org.example.agora.home.HomePage
import org.example.agora.notes.NewLatexCreationPage
import org.example.agora.utils.FileSaver
import org.example.agora.utils.LatexCodeConverter
import org.example.agora.settings.SettingsPage
import org.example.agora.utils.SettingsRepository

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
fun App(
    modifier: Modifier = Modifier,
    settingsRepository: SettingsRepository,
    fileSaver: FileSaver,
    latex: LatexCodeConverter
) {
    var selectedRoute by remember { mutableStateOf("home") }
    var navigator by remember { mutableStateOf(Navigator()) }

    // Stato per controllare la visibilità della NavBar
    var isNavBarVisible by remember { mutableStateOf(true) }

    CompositionLocalProvider(LocalNavigator provides navigator) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = DarkColorPalette.background
        ) {
            // BoxWithConstraints ci permette di leggere la larghezza della finestra in tempo reale
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

                // Chiude automaticamente la NavBar se la finestra stringe sotto gli 800dp
                LaunchedEffect(maxWidth) {
                    isNavBarVisible = maxWidth > 800.dp
                }

                Row(Modifier.fillMaxSize()) {

                    AnimatedVisibility(
                        visible = isNavBarVisible,
                        enter = slideInHorizontally(initialOffsetX = { -it }),
                        exit = slideOutHorizontally(targetOffsetX = { -it })
                    ) {
                        NavBar(
                            selectedRoute = navigator.currentRoute,
                            onRouteSelected = { route ->
                                navigator.navigate(route)
                            },
                            modifier = Modifier.width(240.dp).fillMaxHeight(),
                            SemiDarkColorPalette
                        )
                    }

                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { isNavBarVisible = !isNavBarVisible }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Toggle Menu",
                                    tint = DarkColorPalette.primary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when (navigator.currentRoute) {
                                "home" -> HomePage(
                                    DarkColorPalette,
                                    modifier = Modifier.fillMaxHeight(),
                                    fileSaver,
                                    settingsRepository
                                )

                                "notes" -> NotesPage(
                                    modifier,
                                    DarkColorPalette,
                                    fileSaver,
                                    settingsRepository
                                )

                                "settings" -> SettingsPage(
                                    settingsRepository,
                                    modifier,
                                    DarkColorPalette
                                )

                                "new_page" -> NewNotePage(
                                    modifier,
                                    DarkColorPalette,
                                    settingsRepository,
                                    fileSaver
                                )

                                "editNote" -> NotesEditor(
                                    DarkColorPalette,
                                    modifier,
                                    settingsRepository,
                                    navigator.currentNoteId!!,
                                    fileSaver
                                )

                                "latex" -> LatexEditorPage(
                                    DarkColorPalette,
                                    modifier,
                                    settingsRepository,
                                    fileSaver,
                                    latexCodeConverter = latex
                                )

                                "converter" -> ConverterPage(
                                    DarkColorPalette,
                                    modifier
                                )

                                "newLatexNote" -> NewLatexCreationPage(
                                    modifier,
                                    DarkColorPalette,
                                    settingsRepository,
                                    fileSaver
                                )

                                "notescopy" -> CopyNotesPage(
                                    DarkColorPalette,
                                    modifier,
                                    settingsRepository,
                                    fileSaver
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}