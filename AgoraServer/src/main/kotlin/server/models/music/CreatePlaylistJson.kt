package server.models.music

import kotlinx.serialization.Serializable

@Serializable
class CreatePlaylistJson(
    val title: String
)