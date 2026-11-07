package script

import configuration.TelegramConfiguration
import it.tdlight.Init
import it.tdlight.Log
import it.tdlight.client.*
import it.tdlight.jni.TdApi
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.future.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.time.Duration.Companion.milliseconds

class Button(val text: String, val data: ByteArray?)
class BotMsg(val id: Long, var text: String, var buttons: List<Button>)

sealed interface BotEvent {
    data class Text(val id: Long) : BotEvent
    data class Edited(val id: Long) : BotEvent
    data class Audio(val messageId: Long, val fileId: Int, val title: String, val artist: String, val duration: Int) : BotEvent
}

@Serializable
data class SearchItem(val index: Int, val title: String)
@Serializable
data class SearchResult(val messageId: Long, val items: List<SearchItem>)
@Serializable
data class PickedTrack(val path: String, val title: String, val artist: String, val duration: Int)

class TelegramManager(
    private val cfg: TelegramConfiguration,
    val connection: Database,
    val songsManager: SongsManager
) : AutoCloseable {
    private lateinit var factory: SimpleTelegramClientFactory
    private lateinit var client: SimpleTelegramClient
    private val ready = CompletableDeferred<Unit>()

    @Volatile private var botChatId = 0L
    private val mutex = Mutex()
    private val events = Channel<BotEvent>(Channel.UNLIMITED)
    private val messages = ConcurrentHashMap<Long, BotMsg>()

    private val pendingInputs = ConcurrentHashMap<InputParameter, CompletableFuture<String>>()
    fun waitingFor(): Set<InputParameter> = pendingInputs.keys.toSet()
    fun provideInput(p: InputParameter, value: String): Boolean =
        pendingInputs.remove(p)?.complete(value) ?: false

    fun start() {
        require(cfg.dataDir.isNotBlank()) { "Telegram DataDir mancante" }
        val appId = requireNotNull(cfg.appId) { "Telegram app_id mancante" }
        val apiHash = System.getenv("TG_API_HASH") ?: cfg.apiHash
        val phone = System.getenv("TG_PHONE") ?: cfg.phone

        Init.init()
        Log.setLogMessageHandler(1) { _, _ -> }
        val base = Path(cfg.dataDir).also { it.createDirectories() }
        val settings = TDLibSettings.create(APIToken(appId, apiHash)).apply {
            databaseDirectoryPath = base.resolve("data")
            downloadedFilesDirectoryPath = base.resolve("download")
        }

        factory = SimpleTelegramClientFactory()
        val b = factory.builder(settings)
        b.setClientInteraction { param, _ ->
            when (param) {
                InputParameter.ASK_CODE, InputParameter.ASK_PASSWORD ->
                    CompletableFuture<String>().also { pendingInputs[param] = it }
                else -> CompletableFuture.completedFuture("")
            }
        }
        b.addUpdateHandler(TdApi.UpdateAuthorizationState::class.java) { u ->
            if (u.authorizationState is TdApi.AuthorizationStateReady) ready.complete(Unit)
        }
        b.addUpdateHandler(TdApi.UpdateNewMessage::class.java, ::onNewMessage)
        b.addUpdateHandler(TdApi.UpdateMessageContent::class.java, ::onContentEdited)
        b.addUpdateHandler(TdApi.UpdateMessageEdited::class.java, ::onMarkupEdited)
        b.addUpdateHandler(TdApi.UpdateDeleteMessages::class.java) { u ->
            if (u.chatId == botChatId && u.isPermanent) u.messageIds.forEach { messages.remove(it) }
        }
        client = b.build(AuthenticationSupplier.user(phone))
    }

    suspend fun connect() {
        ready.await()
        botChatId = client.send(TdApi.SearchPublicChat(cfg.bot)).await().id
    }

    // ---------- API pubblica ----------

    suspend fun search(query: String, timeoutMs: Long = 20_000): SearchResult = mutex.withLock {
        drain()
        sendText(query)
        val msg = awaitMessage(timeoutMs) { it.buttons.any(::isTrackButton) }
        SearchResult(msg.id, msg.buttons.mapIndexedNotNull { i, b ->
            if (isTrackButton(b)) SearchItem(i, b.text) else null
        })
    }

    suspend fun pick(messageId: Long, index: Int, timeoutMs: Long = 45_000): PickedTrack = mutex.withLock {
        val msg = messages[messageId] ?: error("Risultati scaduti, rifai la ricerca")
        val btn = msg.buttons.getOrNull(index)?.takeIf(::isTrackButton) ?: error("Indice non valido")
        val data = btn.data ?: error("Bottone senza callback data")
        drain()
        client.send(TdApi.GetCallbackQueryAnswer(botChatId, messageId, TdApi.CallbackQueryPayloadData(data)))
            .await()
        val audio = withTimeout(timeoutMs.milliseconds) {
            var found: BotEvent.Audio? = null
            while (found == null) (events.receive() as? BotEvent.Audio)?.let { found = it }
            found
        }
        val file = client.send(TdApi.DownloadFile(audio.fileId, 32, 0, 0, true)).await()

        transaction(connection){
            songsManager.saveSong(audio.title, audio.artist, audio.duration, file.local.path)
        }

        PickedTrack(file.local.path, audio.title, audio.artist, audio.duration)
    }

    // ---------- interni ----------

    private val trackButtonRegex = Regex("^\\d+\\.\\s")

    private fun isTrackButton(b: Button) = b.data != null && trackButtonRegex.containsMatchIn(b.text)

    private fun drain() { while (events.tryReceive().isSuccess) { } }

    private suspend fun sendText(text: String) {
        val content = TdApi.InputMessageText(TdApi.FormattedText(text, emptyArray()), null, false)
        client.send(TdApi.SendMessage(botChatId, null, null, null, null, content)).await()
    }

    private suspend fun awaitMessage(timeoutMs: Long, ok: (BotMsg) -> Boolean): BotMsg =
        withTimeout(timeoutMs.milliseconds) {
            while (true) {
                val id = when (val e = events.receive()) {
                    is BotEvent.Text -> e.id
                    is BotEvent.Edited -> e.id
                    else -> continue
                }
                messages[id]?.takeIf(ok)?.let { return@withTimeout it }
            }
            @Suppress("UNREACHABLE_CODE") error("unreachable")
        }

    private fun parseButtons(markup: TdApi.ReplyMarkup?): List<Button> =
        (markup as? TdApi.ReplyMarkupInlineKeyboard)?.rows?.flatten()?.map {
            Button(it.text, (it.type as? TdApi.InlineKeyboardButtonTypeCallback)?.data)
        } ?: emptyList()

    private fun onNewMessage(u: TdApi.UpdateNewMessage) {
        val m = u.message
        if (m.chatId != botChatId || m.isOutgoing) return
        when (val c = m.content) {
            is TdApi.MessageText -> {
                if (messages.size > 50) messages.clear()
                messages[m.id] = BotMsg(m.id, c.text.text, parseButtons(m.replyMarkup))
                events.trySend(BotEvent.Text(m.id))
            }
            is TdApi.MessageAudio -> events.trySend(
                BotEvent.Audio(m.id, c.audio.audio.id, c.audio.title, c.audio.performer, c.audio.duration)
            )
            else -> {}
        }
    }

    private fun onContentEdited(u: TdApi.UpdateMessageContent) {
        if (u.chatId != botChatId) return
        val c = u.newContent as? TdApi.MessageText ?: return
        messages[u.messageId]?.let { it.text = c.text.text; events.trySend(BotEvent.Edited(it.id)) }
    }

    private fun onMarkupEdited(u: TdApi.UpdateMessageEdited) {
        if (u.chatId != botChatId) return
        messages[u.messageId]?.let {
            it.buttons = parseButtons(u.replyMarkup)
            events.trySend(BotEvent.Edited(it.id))
        }
    }

    override fun close() {
        client.close()
        factory.close()
    }
}