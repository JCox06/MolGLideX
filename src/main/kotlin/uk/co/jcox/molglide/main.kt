package uk.co.jcox.molglide

import com.formdev.flatlaf.FlatIntelliJLaf
import com.formdev.flatlaf.FlatLightLaf
import com.sun.java.swing.plaf.gtk.GTKLookAndFeel
import com.sun.java.swing.plaf.motif.MotifLookAndFeel
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.plaf.metal.MetalLookAndFeel
import javax.swing.plaf.multi.MultiLookAndFeel

fun main() {

    System.setProperty("sun.java2d.opengl", "true")

    FlatIntelliJLaf.setup()

    JFrame.setDefaultLookAndFeelDecorated(true)
    JDialog.setDefaultLookAndFeelDecorated(false)
    UIManager.put("PopupMenu.consumeEventOnClose", true)

    SwingUtilities.invokeLater {
        val mainData = MainData()
        val mainFrame = MolGlideFrame(mainData)
        val mainController = MainController(mainFrame, mainData)
    }
}