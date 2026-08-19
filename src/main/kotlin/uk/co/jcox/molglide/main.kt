package uk.co.jcox.molglide

import com.formdev.flatlaf.FlatLightLaf
import uk.co.jcox.molglide.mainframe.MainController
import uk.co.jcox.molglide.mainframe.MainData
import uk.co.jcox.molglide.mainframe.MolGlideFrame
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager

fun main() {
    FlatLightLaf.setup()

    JFrame.setDefaultLookAndFeelDecorated(true)
    JDialog.setDefaultLookAndFeelDecorated(false)
    UIManager.put("PopupMenu.consumeEventOnClose", true)

    SwingUtilities.invokeLater {
        val mainData = MainData()
        val mainFrame = MolGlideFrame(mainData)
        val mainController = MainController(mainFrame, mainData)
    }
}