package uk.co.jcox.molglide

import io.github.andrewauclair.moderndocking.app.DockableMenuItem
import io.github.andrewauclair.moderndocking.app.Docking
import org.openscience.cdk.interfaces.IBond
import uk.co.jcox.molglide.editor.control.EditorStateController
import uk.co.jcox.molglide.editor.io.ClipboardMoleculePayload
import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.io.LevelLoader
import uk.co.jcox.molglide.editor.io.LevelSerializer
import uk.co.jcox.molglide.editor.io.MolGLideMetaData
import uk.co.jcox.molglide.editor.ui.EditorPanel
import java.awt.Toolkit
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener
import java.awt.event.WindowEvent
import java.io.File
import java.util.UUID
import javax.swing.JCheckBoxMenuItem
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


    val getControllerFunc: () -> EditorStateController? = {mainData.activeSession?.editorController}
    private val editLabelAction = EditLabelMenuAction(mainFrame, getControllerFunc)
    private val deleteAtomMenuAction = DeleteAtomMenuAction(getControllerFunc)
    private val toggleAtomVisibility = ToggleAtomVisibilityMenuAction(getControllerFunc)
    private val ignoreErrors = IgnoreErrorAction(getControllerFunc)
    private val flipSelectedBond = FlipBondMenuAction(getControllerFunc)
    private val setSingleBondAction = SetPlainBondMenuAction(getControllerFunc)
    private val setHashedBondAction = SetDashedBondMenuAction(getControllerFunc)
    private val setWedgedBondMenuAction = SetWedgedBondMenuAction(getControllerFunc)
    private val flipStereoChemMenuAction = FlipStereoChemMenuAction(getControllerFunc)
    private val setDoubleBondMenuAction = SetDoubleBondMenuAction(getControllerFunc)
    private val setAromaticDoubleBondMenuAction = SetAromaticDoubleBondMenuAction(getControllerFunc)
    private val setTripleBondMenuAction = SetTripleBondMenuAction(getControllerFunc)
    private val deleteBondMenuAction = DeleteBondMenuAction(getControllerFunc)
    private val copySelectionAction = CopySelectionAction(this)
    private val pasteSelectionAction = PasteSelectionAction(this)
    private val cutSelectionAction = CutSelectionAction(this)
    private val deleteSelectionAction = DeleteSelectionAction(getControllerFunc)

    init {

        buildFileMenu()
        buildEditMenu()
        buildObjectMenu()
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


    fun copySelectedMolecules() {
        val activeSession = mainData.activeSession ?: return

        val svgGen = SVGExporter()
        val mgxExporter = LevelSerializer()

        val svgPayload = svgGen.quickExport(activeSession.editorPanel)
        val mgxPayload = mgxExporter.getJSONEncoding(activeSession.editorData, MolGLideMetaData(), activeSession.editorData.selectionManager.batchSelection)
        val filePayload = listOf(MolGLideUtils.writeTempFile(svgPayload))
        val payload = ClipboardMoleculePayload(mgxPayload, svgPayload, filePayload)
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(payload, null)
    }

    fun pasteSelectedMolecules() {

        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        if (! clipboard.isDataFlavorAvailable(ClipboardMoleculePayload.JSON_FLAVOUR)) {
            return
        }
        val mgxData = clipboard.getData(ClipboardMoleculePayload.JSON_FLAVOUR)
        if (mgxData !is String) {
            return
        }
        val levelLoader = LevelLoader()
        val tempLevel = levelLoader.loadLevel(mgxData)

        val session = mainData.activeSession ?: return

        val mx = session.editorData.getLastMouseX()
        val my = session.editorData.getLastMouseY()

        val md = levelLoader.metaData
        session.editorController.importLevel(tempLevel, mx, my, md.copyAtScreenX, md.copyAtScreenY)
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
                updateActionsEnabledStatus(editorSession)

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

    private fun buildObjectMenu() {
        mainFrame.objectMenu.add(editLabelAction)
        mainFrame.objectMenu.add(JCheckBoxMenuItem(toggleAtomVisibility))
        mainFrame.objectMenu.add(deleteAtomMenuAction)
        mainFrame.objectMenu.add(ignoreErrors)
        mainFrame.objectMenu.addSeparator()
        mainFrame.objectMenu.add(flipSelectedBond)
        mainFrame.objectMenu.add(JCheckBoxMenuItem(setSingleBondAction))
        mainFrame.objectMenu.add(JCheckBoxMenuItem(setWedgedBondMenuAction))
        mainFrame.objectMenu.add(JCheckBoxMenuItem(setHashedBondAction))
        mainFrame.objectMenu.add(flipStereoChemMenuAction)
        mainFrame.objectMenu.add(JCheckBoxMenuItem(setDoubleBondMenuAction))
        mainFrame.objectMenu.add(JCheckBoxMenuItem(setAromaticDoubleBondMenuAction))
        mainFrame.objectMenu.add(JCheckBoxMenuItem(setTripleBondMenuAction))
        mainFrame.objectMenu.add(deleteBondMenuAction)
        mainFrame.objectMenu.addSeparator()
        mainFrame.objectMenu.add(copySelectionAction)
        mainFrame.objectMenu.add(pasteSelectionAction)
        mainFrame.objectMenu.add(cutSelectionAction)
        mainFrame.objectMenu.add(deleteSelectionAction)
    }

    private fun buildAboutMenu() {
        mainFrame.helpMenu.add(visitWebsiteAction)
        mainFrame.helpMenu.add(visitRepoAction)
        mainFrame.helpMenu.add(visitBugTrackerAction)
        mainFrame.helpMenu.add(visitAboutMenuAction)
    }


    //todo make an SwingAction Manager to manage all of these actions
    //make it so this is done automatically on the object
    //just to test for now
    private fun updateActionsEnabledStatus(currentSession: EditorSession) {
        val editorController = currentSession.editorController
        val editorData = currentSession.editorData
        val currentAtom = editorData.selectionManager.getAtom()
        val currentBond = editorData.selectionManager.getBond()
        val hasBatch = editorData.selectionManager.hasBatchSelection()

        undoAction.isEnabled = editorController.actionManager.canUndo()
        redoAction.isEnabled = editorController.actionManager.canRedo()

        editLabelAction.isEnabled = currentAtom != null
        deleteAtomMenuAction.isEnabled = currentAtom != null
        toggleAtomVisibility.isEnabled = currentAtom != null
        toggleAtomVisibility.setSelected(currentAtom?.isVisible() ?: true)
        ignoreErrors.isEnabled = currentAtom != null
        ignoreErrors.setSelected(currentAtom?.shouldIgnoreErrors() ?: false)

        flipSelectedBond.isEnabled = currentBond != null
        setSingleBondAction.isEnabled = currentBond != null
        setWedgedBondMenuAction.isEnabled = currentBond != null
        setHashedBondAction.isEnabled = currentBond != null
        setSingleBondAction.setSelected(currentBond?.bond?.order == IBond.Order.SINGLE && currentBond?.stereo() == StereoChem.NORMAL)
        setHashedBondAction.setSelected(currentBond?.bond?.order == IBond.Order.SINGLE && currentBond?.stereo() == StereoChem.DASHED)
        setWedgedBondMenuAction.setSelected(currentBond?.bond?.order == IBond.Order.SINGLE && currentBond?.stereo() == StereoChem.WEDGED)

        flipStereoChemMenuAction.isEnabled = currentBond != null
        setDoubleBondMenuAction.isEnabled = currentBond != null
        setAromaticDoubleBondMenuAction.isEnabled = currentBond != null
        setTripleBondMenuAction.isEnabled = currentBond != null
        deleteBondMenuAction.isEnabled = currentBond != null
        setDoubleBondMenuAction.setSelected(currentBond?.bond?.order == IBond.Order.DOUBLE)
        setAromaticDoubleBondMenuAction.setSelected(currentBond?.bond?.isAromatic ?: false)
        setTripleBondMenuAction.setSelected(currentBond?.bond?.order == IBond.Order.TRIPLE)

        copySelectionAction.isEnabled = hasBatch
        cutSelectionAction.isEnabled = hasBatch
        deleteSelectionAction.isEnabled = hasBatch
    }
}