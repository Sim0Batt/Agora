package server.models.music

import kotlinx.serialization.Serializable

@Serializable
class SearchResultJson(
    val messageId: Int,
    val items: List<SongInfo>
)

@Serializable
class SongInfo(
    val index: Int,
    val title: String
)