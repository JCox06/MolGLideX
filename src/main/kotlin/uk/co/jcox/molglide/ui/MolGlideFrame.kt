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
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JToolBar

class MolGlideFrame : JFrame("MolGLideX (INDEV)") {

    private val windows: MutableList<DockingPanel> = mutableListOf()
    private val appManager: AppManager = AppManager()

    private lateinit var dockRoot: RootDockingPanel

    init {
        this.setSize(1200, 800)
        this.setLocationRelativeTo(null)


        initDocking()
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
        add(Toolbox(appManager), BorderLayout.PAGE_END)
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
    }


    fun getWindows() : List<DockingPanel> {
        return windows
    }



}

