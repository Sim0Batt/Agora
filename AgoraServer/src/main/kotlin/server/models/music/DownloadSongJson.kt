package server.models.music

import kotlinx.serialization.Serializable

@Serializable
class DownloadSongJson(
    val title: String,
    val url: String
)