package configuration

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

class LatexConfiguration {
    @field:Element(name = "CachePath", required = false)
    var cachePath: String = ""

    @field:Element(name = "PdfPath", required = false)
    var pdfPath: String = ""

    @field:Element(name = "Engine", required = false)
    var engine: String = ""

    @field:Element(name = "ImgPath", required = false)
    var imgPath: String = ""
}