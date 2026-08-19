package uk.co.jcox.molglide

import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel

class AboutDialogue(mainFrame: JFrame) : JDialog(mainFrame, "About MolGLideX", ModalityType.APPLICATION_MODAL) {

    init {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = BorderFactory.createEmptyBorder(20, 20, 20, 20)

        val title = JLabel("MolGLideX")
        title.font = title.font.deriveFont(Font.BOLD, 16.0f)

        val desc = JLabel("MolGLideX is in development! This is ALPHA software and unstable")

        val close = JButton("Close")
        close.addActionListener {dispose()}

        val web = JLabel("www.molglide.com")

        panel.add(title)
        panel.add(desc)
        panel.add(web)
        panel.add(close)

        add(panel)
        pack()
        setLocationRelativeTo(mainFrame)
        setResizable(false)
    }
}