package uk.co.jcox.molglide

import java.awt.Color
import java.io.File
import javax.swing.UIManager

object MolGLideUtils {

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
}