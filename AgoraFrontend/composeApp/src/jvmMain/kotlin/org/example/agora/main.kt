package org.example.agora

import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.example.agora.utils.DesktopFileSaver
import org.example.agora.utils.DesktopLatexCodeConverter
import org.example.agora.utils.SettingsRepository

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "agora",
        state = rememberWindowState(
            placement = WindowPlacement.Maximized
        )
    ) {
        val settingsRepository = SettingsRepository()
        val fileSaver = DesktopFileSaver
        App(
            settingsRepository = settingsRepository,
            modifier = Modifier,
            fileSaver = fileSaver,
            latex = DesktopLatexCodeConverter
        )
    }
}