package configuration

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "Server", strict = false)
object ServerConfiguration {
    @field:Element(name = "Host", required = false)
    var host: String = ""

    @field:Element(name = "Port", required = false)
    var port: String = ""
}