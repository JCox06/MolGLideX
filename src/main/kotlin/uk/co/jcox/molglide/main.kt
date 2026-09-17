package uk.co.jcox.molglide

import com.formdev.flatlaf.FlatLightLaf
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager

fun main() {

    System.setProperty("sun.java2d.opengl", "true")

    AppSettings.refreshDataFromDisc()

    try {
        val theme = AppSettings.settings.lookAndFeel
        val className = AppSettings.themes[theme]
        require(className != null) { "Invalid theme type" }
        UIManager.setLookAndFeel(className)
    } catch (e: Exception) {
        FlatLightLaf.setup()
    }

    JFrame.setDefaultLookAndFeelDecorated(true)
    JDialog.setDefaultLookAndFeelDecorated(false)
    UIManager.put("PopupMenu.consumeEventOnClose", true)

    SwingUtilities.invokeLater {
        val mainData = MainData()
        val mainFrame = MolGlideFrame(mainData)
        val mainController = MainController(mainFrame, mainData)
    }
}