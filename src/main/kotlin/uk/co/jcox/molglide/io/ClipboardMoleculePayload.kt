package uk.co.jcox.molglide.io

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.SystemFlavorMap
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.File

//It seems the real key to handle copy and paste with SVG images is to save to a temp file first, and hand that to the
//clipboard content
class ClipboardMoleculePayload (
    private val mgxJsonData: String,
    private val svgImageData: String,
    private val files: List<File>
) : Transferable{

    private val supportedFlavours = arrayOf(SVG_FLAVOUR, INK_SCAPE_FLAVOUR, FILE_FLAVOUR, JSON_FLAVOUR)

    init {
//        val systemFlavourMap = SystemFlavorMap.getDefaultFlavorMap() as SystemFlavorMap
//        systemFlavourMap.addUnencodedNativeForFlavor(SVG_FLAVOUR, "image/svg+xml")
//        systemFlavourMap.addUnencodedNativeForFlavor(INK_SCAPE_FLAVOUR, "image/x-inkscape-svg")

    }

    override fun getTransferDataFlavors(): Array<out DataFlavor?>? {
        return supportedFlavours
    }

    override fun isDataFlavorSupported(flavour: DataFlavor?): Boolean {
        return supportedFlavours.contains(flavour)
    }

    override fun getTransferData(flavour: DataFlavor?): Any {
        if (flavour == JSON_FLAVOUR) {
            return mgxJsonData
        }
        if (flavour == SVG_FLAVOUR || flavour == INK_SCAPE_FLAVOUR) {
            return svgImageData
        }
        if (flavour == FILE_FLAVOUR) {
            return files
        }
        throw UnsupportedFlavorException(flavour)
    }

    companion object {
        val JSON_FLAVOUR = DataFlavor("application/json; class=java.lang.String", "JSON MGX Data") //Note: This is only for JSON MGX data
        val SVG_FLAVOUR = DataFlavor("image/svg+xml; class=java.io.InputStream","Scalable Vector Graphic");
        val INK_SCAPE_FLAVOUR = DataFlavor("image/x-inkscape-svg; class=java.io.InputStream", "Scalable Vector Graphic")
        val FILE_FLAVOUR = DataFlavor.javaFileListFlavor
        //val SVG_TEXT_FLAVOUR = DataFlavor.stringFlavor
    }
}