package org.example.agora

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.example.agora.settings.SettingsRepository

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "agora",
        state = rememberWindowState(
            placement = WindowPlacement.Maximized
        )
    ) {
        val settingsRepository = SettingsRepository()
        App(settingsRepository = settingsRepository)
    }
}