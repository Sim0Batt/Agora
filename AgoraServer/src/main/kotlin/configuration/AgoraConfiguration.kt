package configuration

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name ="Configuration")
class AgoraConfiguration {
    @field:Element(name = "Database", required = true)
    lateinit var databaseConfig: DatabaseConfiguration

    @field:Element(name = "Server", required = true)
    var server: ServerConfiguration? = null

    @field:Element(name = "Telegram", required = true)
    var telegram: TelegramConfiguration? = null
}