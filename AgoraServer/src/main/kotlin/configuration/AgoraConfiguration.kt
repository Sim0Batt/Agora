package configuration

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name ="Configuration")
class AgoraConfiguration {
    @field:Element(name = "Database", required = true)
    lateinit var databaseConfig: DatabaseConfiguration

    @field:Element(name = "Server", required = true)
    var server: ServerConfiguration? = null
}