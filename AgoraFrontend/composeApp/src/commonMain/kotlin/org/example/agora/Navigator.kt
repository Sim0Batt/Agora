package org.example.agora

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

class Navigator {
    var currentRoute by mutableStateOf("home")
        private set

    var currentNoteId by mutableStateOf<Int?>(null)
        private set

    fun navigate(route: String, noteId: Int? = null) {
        currentRoute = route
        currentNoteId = noteId
    }

    fun goHome() {
        currentRoute = "home"
        currentNoteId = null
    }
}

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    error("Navigator non fornito! Hai dimenticato il CompositionLocalProvider?")
}