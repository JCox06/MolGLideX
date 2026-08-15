package uk.co.jcox.molglide

import com.formdev.flatlaf.util.SystemFileChooser
import java.awt.Color
import java.awt.Image
import java.awt.image.ImageFilter
import java.io.File
import javax.swing.JPanel
import javax.swing.UIManager

object MolGLideUtils {

    const val VERSION = "ALPHA-0.0.1"

    private val mgxFilter = SystemFileChooser.FileNameExtensionFilter("MolGLide projects (.mgx)", "mgx")

    fun getMolGLideHome() : File {
        val userHome = File(System.getProperty("user.home"))
        val molglide = File(userHome, ".molglide")
        molglide.mkdir()
        return molglide
    }

    fun getQuickCaptureDirectory() : File {
        val molglide = getMolGLideHome()
        val captures = File(molglide, "captures")
        captures.mkdir()
        return captures
    }

    fun getAccentColour() : Color {
        return UIManager.getColor("Component.accentColor") ?: Color.PINK
    }


    /**
     * Presents the user with a dialogue to choose where to the file to
     * @return the file that the state should be saved to
     */
    fun showSaveDialogue(parent: JPanel): File? {
        val fileChooser = SystemFileChooser()
        fileChooser.addChoosableFileFilter(mgxFilter)
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
        fileChooser.showOpenDialog(null)
        val file = fileChooser.selectedFile
        return file
    }
}