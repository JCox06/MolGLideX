package uk.co.jcox.molglide

import com.formdev.flatlaf.util.SystemFileChooser
import org.openscience.cdk.silent.SilentChemObjectBuilder
import uk.co.jcox.molglide.editor.model.chemengine.CDKContainerWrapper
import uk.co.jcox.molglide.editor.model.chemengine.MgxMolecule
import java.awt.Color
import java.awt.Component
import java.io.File
import java.util.*
import javax.swing.UIManager

object MolGLideUtils {

    const val VERSION = "ALPHA-0.0.2"
    const val WEBSITE = "https://molglide.com/"
    const val REPO = "https://github.com/JCox06/MolGLideX"
    const val BUG_TRACKER = "https://github.com/JCox06/MolGLideX/issues"

    private val mgxFilter = SystemFileChooser.FileNameExtensionFilter("MolGLide projects (.mgx)", "mgx")

    fun getMolGLideHome() : File {
        val userHome = File(System.getProperty("user.home"))
        val molglide = File(userHome, ".molglide")
        molglide.mkdir()
        return molglide
    }

    fun getMolGLideSaveLocation(): File {
        val userDocuments = File(System.getProperty("user.home") + "/Documents/MolGLideX")
        if (!userDocuments.exists()) {
            userDocuments.mkdir()
        }
        return userDocuments
    }

    fun getAccentColour() : Color {
        return UIManager.getColor("Component.accentColor") ?: Color.PINK
    }

    fun getFocusColour() : Color {
        return UIManager.getColor("Component.focusColor") ?: Color.PINK
    }


    /**
     * Presents the user with a dialogue to choose where to the file to
     * @return the file that the state should be saved to
     */
    fun showSaveDialogue(parent: Component): File? {
        val fileChooser = SystemFileChooser()
        fileChooser.addChoosableFileFilter(mgxFilter)
        fileChooser.currentDirectory = getMolGLideSaveLocation()
        fileChooser.showSaveDialog(parent)
        val file = fileChooser.selectedFile
        if (file != null && file.extension.isEmpty()) {
            return File("${file}.mgx")
        }

        return file
    }

    fun showOpenDialogue() : File? {
        val fileChooser = SystemFileChooser()
        fileChooser.addChoosableFileFilter(mgxFilter)
        fileChooser.currentDirectory = getMolGLideSaveLocation()
        fileChooser.showOpenDialog(null)
        val file = fileChooser.selectedFile
        return file
    }


    fun writeTempFile(content: String) : File {
        val temp = getTempFile()
        temp.writeText(content)
        return temp
    }

    fun getTempFile(): File {
        val temp = File.createTempFile("MGX", ".svg")
        temp.deleteOnExit()
        return temp
    }

    fun createMolecule(): MgxMolecule {
        val chemBuilder = SilentChemObjectBuilder.getInstance()
        val cdkContainer = chemBuilder.newAtomContainer()
        val mgxMolecule = CDKContainerWrapper(cdkContainer)
        return mgxMolecule
    }

    fun getSvgAsBase64HTML(svg: String): String {
        val base64 = Base64.getEncoder().encodeToString(svg.toByteArray())

        val htmlString = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<title>MolGLide Export</title>\n" +
                "<body>\n" +
                "\n" +
                "<img alt=\"SVG generated image via MolGLide\" src=\"data:image/svg+xml;base64,${base64}\" />\n" +
                "\n" +
                "</body>" +
                "</html>"
        return htmlString
    }
}