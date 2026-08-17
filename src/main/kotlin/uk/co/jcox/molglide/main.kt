package uk.co.jcox.molglide

import com.formdev.flatlaf.FlatDarculaLaf
import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatIntelliJLaf
import com.formdev.flatlaf.FlatLightLaf
import com.formdev.flatlaf.themes.FlatMacDarkLaf
import com.formdev.flatlaf.themes.FlatMacLightLaf
import com.formdev.flatlaf.ui.FlatUIUtils
import uk.co.jcox.molglide.ui.MolGlideFrame
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager

fun main() {
    FlatDarkLaf.setup()

    JFrame.setDefaultLookAndFeelDecorated(true)
    JDialog.setDefaultLookAndFeelDecorated(false)
    UIManager.put("PopupMenu.consumeEventOnClose", true)

    SwingUtilities.invokeLater {
        val mainFrame = MolGlideFrame()
    }
}