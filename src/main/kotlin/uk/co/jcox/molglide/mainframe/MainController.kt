package uk.co.jcox.molglide.mainframe

import io.github.andrewauclair.moderndocking.app.DockableMenuItem
import io.github.andrewauclair.moderndocking.app.Docking
import org.apache.jena.sparql.function.library.date
import uk.co.jcox.molglide.control.EditorStateController
import uk.co.jcox.molglide.control.EditorStateData
import uk.co.jcox.molglide.io.LevelLoader
import uk.co.jcox.molglide.io.LevelSerializer
import uk.co.jcox.molglide.main
import uk.co.jcox.molglide.ui.DockingPanel
import uk.co.jcox.molglide.ui.EditorPanel
import uk.co.jcox.molglide.ui.LoadFileAction
import uk.co.jcox.molglide.ui.NewProjectAction
import uk.co.jcox.molglide.ui.RedoAction
import uk.co.jcox.molglide.ui.SaveAsFileAction
import uk.co.jcox.molglide.ui.SaveFileAction
import uk.co.jcox.molglide.ui.UndoAction
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener
import java.awt.event.WindowEvent
import java.io.File
import java.util.UUID
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.event.MenuEvent
import javax.swing.event.MenuListener

class MainController (
    private val mainFrame: MolGlideFrame,
    private val mainData: MainData,
) {

    init {
        mainFrame.fileMenu.add(NewProjectAction(this))
        mainFrame.fileMenu.add(LoadFileAction(this, mainFrame))
        mainFrame.fileMenu.add(SaveFileAction(this))
        mainFrame.fileMenu.add(SaveAsFileAction(this, mainFrame))

        mainFrame.editMenu.add(UndoAction(this))
        mainFrame.editMenu.add(RedoAction(this))

        mainFrame.windowMenu.addMenuListener(object : MenuListener {
            override fun menuSelected(e: MenuEvent?) {
                mainFrame.windowMenu.removeAll()
                mainData.modernDockingManaged.forEach { win ->
                    val item = DockableMenuItem(win.persistentID, win.internalText)
                    mainFrame.windowMenu.add(item)
                }
            }
            override fun menuDeselected(e: MenuEvent?) {}
            override fun menuCanceled(e: MenuEvent?) {}
        })

        mainFrame.toolBox.registerEditModeCallback { editMode ->
            mainData.editToolMode = editMode
        }
    }

    fun newProject() {
        val newData = EditorStateData()
        createSession(newData, null)
    }

    fun openProject(file: File) {
        val levelLoader = LevelLoader()
        val data = levelLoader.loadLevel(file)
        createSession(data, file)
    }

    fun saveActiveProject() {
        val session = mainData.activeSession ?: return
        val saveFile = session.saveFile
        if (saveFile == null) {
            val saveAsAction = SaveAsFileAction(this, mainFrame)
            saveAsAction.actionPerformed(null)
        }
        val levelSerializer = LevelSerializer()
        val json = levelSerializer.getJSONEncoding(session.editorData)
        saveFile?.writeText(json)
    }

    fun saveActiveProjectAs(file: File) {
        mainData.activeSession?.saveFile = file
        saveActiveProject()
    }

    private fun createSession(data: EditorStateData, file: File? = null) {
        val editorPanel = EditorPanel(data)
        val editorController = EditorStateController(mainData, data, editorPanel)
        val session = EditorSession(UUID.randomUUID().toString(), data, editorController, editorPanel, file)
        mainData.sessions.add(session)
        var tabname = "Untitled Document ${mainData.sessions.size}"
        if (file != null) tabname = file.name

        manageByDocking(editorPanel, session.id, tabname)
        createAlertListener(session)
    }

    fun manageByDocking(swingComp: JComponent, id: String, tabtext: String) {
        val dockingPanel = DockingPanel(id, tabtext)
        dockingPanel.add(swingComp)
        mainData.modernDockingManaged.add(dockingPanel)

        Docking.dock(dockingPanel, mainFrame)
        Docking.display(dockingPanel)
    }


    //Listens for mouse movement on the editor panel
    //The most recent panel to have mouse movement is registered as the active panel
    private fun createAlertListener(editorSession: EditorSession) {
        editorSession.editorPanel.addMouseMotionListener(object : MouseMotionListener {
            override fun mouseDragged(e: MouseEvent?) {
                alert()
            }

            override fun mouseMoved(e: MouseEvent?) {
                alert()
            }

            fun alert() {
                mainData.activeSession = editorSession
                mainFrame.updateStatusBar()
            }
        })
    }

    fun shutdown() {
        mainFrame.dispatchEvent(WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING))
    }

    fun handleGlobalUndo() {
        mainData.activeSession?.editorController?.actionManager?.undoLastAction()
    }

    fun handleGlobalRedo() {
        mainData.activeSession?.editorController?.actionManager?.restoreLastAction()
    }

}