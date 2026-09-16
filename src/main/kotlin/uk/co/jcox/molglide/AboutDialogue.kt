package uk.co.jcox.molglide

import java.awt.Font
import java.io.File
import javax.swing.*

class AboutDialogue(mainFrame: JFrame) : JDialog(mainFrame, "About MolGLideX", ModalityType.APPLICATION_MODAL) {

    init {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = BorderFactory.createEmptyBorder(20, 20, 20, 20)

        val title = JLabel("MolGLideX")
        title.font = title.font.deriveFont(Font.BOLD, 16.0f)

        val textArea = JTextArea()
        textArea.lineWrap = true
//        textArea.wrapStyleWord = true
        textArea.text = File("CONTRIBUTORS.txt").readText()
        textArea.rows = 5
        textArea.isEditable = false
        val scrollBar = JScrollPane(textArea)

        val desc = JLabel("MolGLideX is in development! This is ALPHA software and unstable")


        val close = JButton("Close")
        close.addActionListener {dispose()}

        val web = JLabel("www.molglide.com")

        panel.add(title)
        panel.add(desc)
        panel.add(scrollBar)
        panel.add(web)
        panel.add(close)

        add(panel)
        pack()
        setLocationRelativeTo(mainFrame)
        setResizable(false)
    }
}