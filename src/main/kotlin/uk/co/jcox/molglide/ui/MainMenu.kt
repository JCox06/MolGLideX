package uk.co.jcox.molglide.ui

import io.github.andrewauclair.moderndocking.app.DockableMenuItem
import sun.tools.jconsole.AboutDialog
import uk.co.jcox.molglide.control.AppManager
import uk.co.jcox.molglide.control.EditorStateController
import java.awt.Desktop
import java.awt.event.WindowEvent
import java.net.URI
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JToolBar
import javax.swing.event.MenuEvent
import javax.swing.event.MenuListener

class MainMenu(val mainFrame: MolGlideFrame, val appManager: AppManager) : JMenuBar() {

    init {
        add(buildFileMenu())
        add(buildEditMenu())
        add(buildWindowMenu())
        add(buildHelpMenu())
    }

    private fun buildFileMenu() : JMenu {
        val menuFile = JMenu("File")

        menuFile.add(NewProjectAction(mainFrame, appManager))

        menuFile.addSeparator()

        menuFile.add(QuickCaptureAction(appManager))

        menuFile.addSeparator()

        menuFile.add(JMenuItem(abstractAction("Visit Website") {Desktop.getDesktop().browse(URI("https://molglide.com"))}))

        menuFile.add(JMenuItem(abstractAction("Report Bugs") {Desktop.getDesktop().browse(URI("https://molglide.com/bugs"))}))

        menuFile.addSeparator()
        menuFile.add(QuitAction(mainFrame))
        return menuFile
    }

    private fun buildEditMenu() : JMenu {
        val menuEdit = JMenu("Edit")

        menuEdit.add(UndoAction(appManager))
        menuEdit.add(RedoAction(appManager))
        return menuEdit
    }

    private fun buildWindowMenu() : JMenu {
        val menuWindows = JMenu("Windows")

        menuWindows.addMenuListener(object : MenuListener {
            override fun menuSelected(e: MenuEvent?) {

                menuWindows.removeAll()
                menuWindows.add(JMenuItem("Hide All"))
                menuWindows.add(JMenuItem("Show All"))

                menuWindows.addSeparator()

                mainFrame.getWindows().forEach { win ->
                    val item = DockableMenuItem(win.persistentID, win.tabText)
                    menuWindows.add(item)
                }
            }

            override fun menuDeselected(e: MenuEvent?) {

            }

            override fun menuCanceled(e: MenuEvent?) {

            }
        })
        return menuWindows
    }

    private fun buildHelpMenu() : JMenu {
        val menuHelp = JMenu("Help")

        val about = JMenuItem("About")
        about.addActionListener {
            val dialogue = AboutDialogue(mainFrame)
            dialogue.isVisible = true
        }

        menuHelp.add(about)

        return menuHelp
    }
}