package uk.co.jcox.molglide.control

import org.apache.batik.dom.GenericDOMImplementation
import org.apache.batik.svggen.SVGGraphics2D
import uk.co.jcox.molglide.ui.EditorPanel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

class SVGExporter {

    fun quickExport(panel: EditorPanel, file: File) {
        val domImpl = GenericDOMImplementation.getDOMImplementation()
        val svgNS = "http://www.w3.org/2000/svg"

        val document = domImpl.createDocument(svgNS, "svg", null)

        val svgGenerator = SVGGraphics2D(document)
        panel.paint(svgGenerator)

        val writer = OutputStreamWriter(FileOutputStream(file))
        svgGenerator.stream(writer)
    }
}