package configuration

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

class TelegramConfiguration {
    @field:Element(name = "app_id", required = false)
    var appId: Int? = -1

    @field:Element(name = "api_hash", required = false)
    var apiHash: String = ""

    @field:Element(name = "DataDir", required = false)
    var dataDir: String = ""

    @field:Element(name = "phone", required = false)
    var phone: String = ""

    @field:Element(name = "bot", required = false)
    var bot: String = "DeezerMusicBot"
}