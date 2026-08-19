package uk.co.jcox.molglide.mainframe

import com.formdev.flatlaf.extras.FlatSVGIcon
import io.github.andrewauclair.moderndocking.app.Docking
import io.github.andrewauclair.moderndocking.app.RootDockingPanel
import io.github.andrewauclair.moderndocking.ext.ui.DockingUI
import uk.co.jcox.molglide.EditMode
import uk.co.jcox.molglide.MolGLideUtils
import uk.co.jcox.molglide.ui.DockingPanel
import uk.co.jcox.molglide.ui.NewProjectAction
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.time.LocalDate
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.SwingUtilities

class MolGlideFrame (
    private val appData: IMainAppData,
) : JFrame("MolGLideX ${LocalDate.now()}") {

    private val menuBar = JMenuBar()
    val fileMenu = JMenu("File")
    val editMenu = JMenu("Edit")
    val windowMenu = JMenu("Windows")
    val helpMenu = JMenu("Help")


    val toolBox = Toolbox(appData)

    private lateinit var dockRoot: RootDockingPanel
    private val statusLabel = JLabel()

    init {
        this.setSize(1200, 800)
        this.setLocationRelativeTo(null)

        val svgImg = FlatSVGIcon("uk/co/jcox/molglide/app_logo.svg", javaClass.classLoader)
        iconImage = svgImg.derive(32, 32).image

        initDocking()
        add(toolBox, BorderLayout.PAGE_START)
        add(buildStatusBar(), BorderLayout.PAGE_END)
        addQuitHandler()


        menuBar.add(fileMenu)
        menuBar.add(editMenu)
        menuBar.add(windowMenu)
        menuBar.add(helpMenu)
        jMenuBar = menuBar


        this.isVisible = true

        JOptionPane.showMessageDialog(this, "MolGLideX is a continuation of MolGLide (legacy edition). MolGLideX is in development and unstable and crashes and bugs are expected to occur", "INDEV", JOptionPane.WARNING_MESSAGE)
    }

    private fun initDocking() {
        Docking.initialize(this)
        DockingUI.initialize()
        dockRoot = RootDockingPanel(this)
        add(dockRoot, BorderLayout.CENTER)

    }

    private fun addQuitHandler() {
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE)
        this.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                if (confirmClose()) {
                    setDefaultCloseOperation(EXIT_ON_CLOSE)
                }
                super.windowClosing(e)
            }
        })
    }

    private fun confirmClose() : Boolean {
        //First ask the user to if they want to quit and show them the open documents
        val panel = JPanel(BorderLayout())
        panel.add(JLabel("Please review your open documents before quiting the application"), BorderLayout.NORTH)

        //todo Need to show a table of all the open documents
        //Query the controller of each document, and ask it if the actionManager is dirty
        //Display all documents in a table, with the name of the document, followed by a save button (if its dirty) or green text that says "document saved" if not
        //Clicking the button starts a save action
        val result = JOptionPane.showConfirmDialog(this, panel, "Quit MolGLide ?", JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE)

        return true
    }


    private fun buildStatusBar() : JPanel {
        val statusBar = JPanel()
        statusBar.setLayout(BorderLayout())
        statusBar.add(statusLabel, BorderLayout.WEST)
        val label = JLabel("MolGLideX ${MolGLideUtils.VERSION}")
        statusBar.add(label, BorderLayout.EAST)
        return statusBar
    }

    fun updateStatusBar() {
        SwingUtilities.invokeLater {
            val weight = String.format("%.4f", appData.getSelectedWeight())
            val hybrid = appData.getSelectedHybrid()
            val formula = appData.getSelectedFormula()
            statusLabel.text = "${formula} | ${weight} g/mol | ${hybrid}"
        }
    }

}

