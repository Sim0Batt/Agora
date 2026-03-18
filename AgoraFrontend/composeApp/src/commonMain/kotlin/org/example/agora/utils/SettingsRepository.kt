package org.example.agora.utils
import com.russhwolf.settings.Settings

class SettingsRepository {
    private val settings: Settings = Settings()

    private companion object {
        const val SAVE_PATH_KEY = "save_path"
        const val AGORA_USER_ID = "agora_user_id"
        const val SAVE_PDF_PATH = "save_pdf_path"
        const val SAVE_TMP_PATH = "save_tmp_path"
        const val LAST_LATEX_NOTE_ID = "last_latex_note_id"
    }

    fun setSavePath(path: String) {
        settings.putString(SAVE_PATH_KEY, path)
    }

    fun getSavePathFlow(defaultPath: String): String {
        return settings.getString(SAVE_PATH_KEY, defaultPath)
    }

    fun setUserId(userId: String) {
        settings.putString(AGORA_USER_ID, userId)
    }

    fun getSavedUserId(): String {
        return settings.getString(AGORA_USER_ID, "1")
    }

    fun savePdfPath(pdfPath: String) {
        settings.putString(SAVE_PDF_PATH, pdfPath)
    }

    fun getPdfPath(defaultPath: String): String {
        return settings.getString(SAVE_PDF_PATH, defaultPath)
    }

    fun saveTmpPath(tmpPath: String) {
        settings.putString(SAVE_TMP_PATH, tmpPath)
    }

    fun getTmpPath(defaultPath: String): String {
        return settings.getString(SAVE_TMP_PATH, defaultPath)
    }

    fun saveLastLatexId(lastId: Int) {
        settings.putInt(LAST_LATEX_NOTE_ID, lastId)
    }

    fun getLastLatexId(): Int {
        return settings.getInt(LAST_LATEX_NOTE_ID, -1)
    }
}