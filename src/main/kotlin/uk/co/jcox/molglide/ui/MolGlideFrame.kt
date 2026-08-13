package uk.co.jcox.molglide.ui

import io.github.andrewauclair.moderndocking.DockingProperty
import io.github.andrewauclair.moderndocking.DockingRegion
import io.github.andrewauclair.moderndocking.app.Docking
import io.github.andrewauclair.moderndocking.app.RootDockingPanel
import io.github.andrewauclair.moderndocking.ext.ui.DockingUI
import jdk.internal.vm.ThreadContainers.root
import uk.co.jcox.molglide.control.AppManager
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JToolBar
import javax.swing.SwingUtilities

class MolGlideFrame : JFrame("MolGLideX (INDEV)") {

    private val windows: MutableList<DockingPanel> = mutableListOf()
    private val appManager: AppManager = AppManager()

    private lateinit var dockRoot: RootDockingPanel

    private val statusLabel = JLabel()

    init {
        this.setSize(1200, 800)
        this.setLocationRelativeTo(null)


        initDocking()
        add(Toolbox(appManager), BorderLayout.PAGE_START)
        add(buildStatusBar(), BorderLayout.PAGE_END)
        addQuitHandler()
        registerDefaultWindows()

        jMenuBar = MainMenu(this, appManager)
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
        return true
    }

    private fun registerDefaultWindows() {
        addDockingPanel("debug1", "DummyEditor1", JPanel(BorderLayout()))
        addDockingPanel("debug2", "DummyEditor2", JPanel(BorderLayout()))
        addDockingPanel("debug3", "DummyEditor3", JPanel(BorderLayout()))
    }


    fun addDockingPanel(persistentID: String, tabText: String, component: JComponent, enable: Boolean = false) {
        val panel = DockingPanel(persistentID, tabText)
        panel.add(component)
        windows.add(panel)
        if (enable) {
            Docking.dock(panel, this)
        }
        component.addMouseMotionListener(object : MouseAdapter() {
            override fun mouseMoved(e: MouseEvent?) {
                handle()
            }
            override fun mouseDragged(e: MouseEvent?) {
                handle()
            }

            fun handle() {
                SwingUtilities.invokeLater {
                    val act = appManager.activeTab
                    if (act != null) {
                        val weight = String.format("%.4f", act.uiBuilder.getSelectedWeight())
                        statusLabel.text = "${act.uiBuilder.getSelectedFormula()} | ${weight}"
                    }
                }
            }
        })
    }

    private fun buildStatusBar() : JPanel {
        val statusBar = JPanel()
        statusBar.setLayout(BorderLayout())
        statusBar.add(statusLabel, BorderLayout.WEST)

        val label = JLabel("MolGLideX (v0.0.1)")
        statusBar.add(label, BorderLayout.EAST)

        return statusBar
    }

    fun getWindows() : List<DockingPanel> {
        return windows
    }

}

