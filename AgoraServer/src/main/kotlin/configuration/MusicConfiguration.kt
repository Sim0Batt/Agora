package configuration

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

class MusicConfiguration {
    @field:Element(name = "YDTEnginePath", required = false)
    var ydtEnginePath: String = ""

    @field:Element(name = "SavePath", required = false)
    var saveFolder: String = ""
}