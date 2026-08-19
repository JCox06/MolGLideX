package uk.co.jcox.molglide

import io.github.andrewauclair.moderndocking.app.DockableMenuItem
import io.github.andrewauclair.moderndocking.app.Docking
import uk.co.jcox.molglide.editor.control.EditorStateController
import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.io.LevelLoader
import uk.co.jcox.molglide.editor.io.LevelSerializer
import uk.co.jcox.molglide.editor.ui.EditorPanel
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener
import java.awt.event.WindowEvent
import java.io.File
import java.util.UUID
import javax.swing.JComponent
import javax.swing.event.MenuEvent
import javax.swing.event.MenuListener

class MainController (
    private val mainFrame: MolGlideFrame,
    private val mainData: MainData,
) : IEditorSessionOrganiser {

    private val newProjectAction = NewProjectAction(this)
    private val loadFileAction = LoadFileAction(this, mainFrame)
    private val saveFileAction = SaveFileAction(this)
    private val saveAsFileAction = SaveAsFileAction(this, mainFrame)
    private val quitAction = QuitAction(this)

    private val undoAction = UndoAction(this)
    private val redoAction = RedoAction(this)

    private val visitWebsiteAction = VisitWebsite()
    private val visitRepoAction = VisitRepoAction()
    private val visitBugTrackerAction = VisitBugTrackerAction()
    private val visitAboutMenuAction = ShowAboutMenuAction(mainFrame)


    init {

        buildFileMenu()
        buildEditMenu()
        buildAboutMenu()

        mainFrame.windowMenu.addMenuListener(object : MenuListener {
            override fun menuSelected(e: MenuEvent?) {
                mainFrame.windowMenu.removeAll()
                mainData.modernDockingManaged.values.forEach { win ->
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

        newProject()
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

        if (saveFile != null) {
            saveFile.writeText(json)
            val dockingPanel = mainData.modernDockingManaged[mainData.activeSession?.editorData?.sessionID]
            dockingPanel?.internalText = saveFile.name
            Docking.updateTabInfo(dockingPanel)
        }
    }

    fun saveActiveProjectAs(file: File) {
        mainData.activeSession?.saveFile = file
        saveActiveProject()
    }

    private fun createSession(data: EditorStateData, file: File? = null) {
        val editorPanel = EditorPanel(data)
        val editorController = EditorStateController(mainData, data, editorPanel, this)
        val randomID = UUID.randomUUID().toString()
        data.sessionID = randomID
        val session = EditorSession(data, editorController, editorPanel, file)
        mainData.sessions[randomID] = session
        var tabname = "Untitled Document ${mainData.sessions.size}"
        if (file != null) tabname = file.name

        manageByDocking(editorPanel, randomID, tabname)
        createAlertListener(session)
    }

    fun manageByDocking(swingComp: JComponent, id: String, tabtext: String) {
        val dockingPanel = DockingPanel(id, tabtext)
        dockingPanel.add(swingComp)
        mainData.modernDockingManaged[id] = (dockingPanel)

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

                //Also check if the current session can undo/redo
                undoAction.isEnabled = editorSession.editorController.actionManager.canUndo()
                redoAction.isEnabled = editorSession.editorController.actionManager.canRedo()

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

    //The other sessions can notify the main controller through the ISessionOrganiser interface
    override fun onDocumentDirty(sessionID: String) {
        //If the document becomes dirty after saving - Display a * in tab text
        val dockPanel = mainData.modernDockingManaged[sessionID]
        val oldText = dockPanel?.internalText
        if (oldText != null && oldText.startsWith("*")) {
            return
        }
        val newText = "*${oldText}"
        dockPanel?.internalText = newText
        Docking.updateTabInfo(sessionID)
    }


    private fun buildFileMenu() {
        mainFrame.fileMenu.add(newProjectAction)
        mainFrame.fileMenu.add(loadFileAction)
        mainFrame.fileMenu.add(saveFileAction)
        mainFrame.fileMenu.add(saveAsFileAction)
        mainFrame.fileMenu.add(quitAction)
    }

    private fun buildEditMenu() {
        mainFrame.editMenu.add(undoAction)
        mainFrame.editMenu.add(redoAction)
    }

    private fun buildAboutMenu() {
        mainFrame.helpMenu.add(visitWebsiteAction)
        mainFrame.helpMenu.add(visitRepoAction)
        mainFrame.helpMenu.add(visitBugTrackerAction)
        mainFrame.helpMenu.add(visitAboutMenuAction)
    }
}