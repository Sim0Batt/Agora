package org.example.agora.settings
import com.russhwolf.settings.Settings
import kotlin.time.Clock

class SettingsRepository {
    private val settings: Settings = Settings()

    private companion object {
        const val SAVE_PATH_KEY = ""
        const val AGORA_USER_ID = ""
    }

    fun setSavePath(path: String) {
        settings.putString(SAVE_PATH_KEY, path)
    }

    fun getSavePathFlow(defaultPath: String): String{
        return settings.getString(SAVE_PATH_KEY, defaultPath)
    }

    fun setUserId(userId: String) {
        settings.putString(AGORA_USER_ID, userId)
    }

    fun getSavedUserId(): String{
        return settings.getString(AGORA_USER_ID, "1")
    }
}