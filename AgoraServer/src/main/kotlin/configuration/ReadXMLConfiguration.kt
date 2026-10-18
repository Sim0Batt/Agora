package configuration

import org.simpleframework.xml.core.Persister
import java.io.File

var mainPath : String = "/home/gaia/etc/supervisor/settings.xml"
var macPath : String = "/Users/Simone/workspace/etc/agora/settings.xml"

var linuxPath : String = "/home/simone/workspace/etc/agora/settings.xml"


object ReadXMLConfiguration {
    fun getConfiguration() : AgoraConfiguration {
        val xmlFile = if(File(mainPath).exists()) {
            File(mainPath)
        }else if ( File(macPath).exists()){
            File(macPath)
        }else if ( File(linuxPath).exists()){
            File(linuxPath)
        }else{
            throw Exception("Configuration Read: File Not Found")
        }
        val serializer = Persister()
        val configuration = serializer.read(AgoraConfiguration::class.java, xmlFile)

        return configuration
    }
}

