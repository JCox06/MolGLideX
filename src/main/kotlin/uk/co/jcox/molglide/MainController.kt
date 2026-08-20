package uk.co.jcox.molglide

import io.github.andrewauclair.moderndocking.app.DockableMenuItem
import io.github.andrewauclair.moderndocking.app.Docking
import jdk.internal.org.jline.terminal.Terminal
import org.openscience.cdk.interfaces.IBond
import org.openscience.cdk.smiles.smarts.parser.SMARTSParserConstants.a
import uk.co.jcox.molglide.editor.control.EditorStateController
import uk.co.jcox.molglide.editor.io.ClipboardMoleculePayload
import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.io.LevelLoader
import uk.co.jcox.molglide.editor.io.LevelSerializer
import uk.co.jcox.molglide.editor.io.MolGLideMetaData
import uk.co.jcox.molglide.editor.ui.EditorPanel
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
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

    private val actionRegistry: SwingActionRegistry = SwingActionRegistry()
    val getControllerFunc: () -> EditorStateController? = {mainData.activeSession?.editorController}


    init {

        registerGlobalActions()
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

    fun copyAsSmiles() {
        val activeSession = mainData.activeSession ?: return
        val s = activeSession.editorData.selectionManager
        val molecule = s.getMolecule() ?: return
        val smiles = molecule.getCanonicalString()
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(smiles), null)
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

                //Also check what actions the current session can do
                val sm = editorSession.editorData.selectionManager
                actionRegistry.stateHasChanged(editorSession, sm.getBond(), sm.getAtom())

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


    private fun registerGlobalActions() {
        actionRegistry.registerAction(NEW_PROJECT_ACTION, NewProjectAction(this))
        actionRegistry.registerAction(LOAD_PROJECT_ACTION, LoadFileAction(this, mainFrame))
        actionRegistry.registerAction(SAVE_PROJECT_ACTION, SaveFileAction(this))
        actionRegistry.registerAction(SAVE_PROJECT_AS_ACTION, SaveAsFileAction(this, mainFrame))
        actionRegistry.registerAction(QUIT_APPLICATION_ACTION, QuitAction(this))

        actionRegistry.registerAction(UNDO_ACTION, UndoAction(this))
        actionRegistry.registerAction(REDO_ACTION, RedoAction(this))

        actionRegistry.registerAction(VISIT_WEBSITE_ACTION, VisitWebsite())
        actionRegistry.registerAction(VISIT_REPO_ACTION, VisitRepoAction())
        actionRegistry.registerAction(VISIT_ISSUE_TRACKER_ACTION, VisitBugTrackerAction())
        actionRegistry.registerAction(VISIT_ABOUT_ACTION, ShowAboutMenuAction(mainFrame))

        actionRegistry.registerAction(EDIT_LABEL_ACTION, EditLabelMenuAction(mainFrame, getControllerFunc))
        actionRegistry.registerAction(DELETE_ATOM_MENU_ACTION, DeleteAtomMenuAction(getControllerFunc))
        actionRegistry.registerAction(TOGGLE_ATOM_VISIBILITY_ACTION, ToggleAtomVisibilityMenuAction(getControllerFunc))
        actionRegistry.registerAction(IGNORE_VALENCY_ERRORS_ACTION, IgnoreErrorAction(getControllerFunc))
        actionRegistry.registerAction(FLIP_SELECTED_ACTION, FlipBondMenuAction(getControllerFunc))
        actionRegistry.registerAction(SET_SINGLE_BOND_ACTION, SetPlainBondMenuAction(getControllerFunc))
        actionRegistry.registerAction(SET_WEDGED_ACTION, SetWedgedBondMenuAction(getControllerFunc))
        actionRegistry.registerAction(SET_HASHED_ACTION, SetDashedBondMenuAction(getControllerFunc))
        actionRegistry.registerAction(FLIP_STEREO_CHEM_ACTION, FlipStereoChemMenuAction(getControllerFunc))
        actionRegistry.registerAction(SET_DOUBLE_BOND_ACTION, SetDoubleBondMenuAction(getControllerFunc))
        actionRegistry.registerAction(SET_AROMATIC_ACTION, SetAromaticDoubleBondMenuAction(getControllerFunc))
        actionRegistry.registerAction(SET_TRIPLE_BOND_ACTION, SetTripleBondMenuAction(getControllerFunc))
        actionRegistry.registerAction(DELETE_BOND_ACTION, DeleteBondMenuAction(getControllerFunc))
        actionRegistry.registerAction(COPY_SELECTION_ACTION, CopySelectionAction(this))
        actionRegistry.registerAction(PASTE_SELECTION_ACTION, PasteSelectionAction(this))
        actionRegistry.registerAction(CUT_SELECTION_ACTION, CutSelectionAction(this))
        actionRegistry.registerAction(DELETE_SELECTION_ACTION, DeleteSelectionAction(getControllerFunc))

        actionRegistry.registerAction(CDK_CLEANUP_STRUCTURE, CDKCleanupStructure(getControllerFunc))
        actionRegistry.registerAction(CDK_COPY_CANONICAL_SMILES_ACTION, CDKCopyCanonicalSmilesAction(this))
    }

    private fun buildFileMenu() {
        mainFrame.fileMenu.add(actionRegistry[NEW_PROJECT_ACTION])
        mainFrame.fileMenu.add(actionRegistry[LOAD_PROJECT_ACTION])
        mainFrame.fileMenu.add(actionRegistry[SAVE_PROJECT_ACTION])
        mainFrame.fileMenu.add(actionRegistry[SAVE_PROJECT_AS_ACTION])
        mainFrame.fileMenu.add(actionRegistry[QUIT_APPLICATION_ACTION])
    }

    private fun buildEditMenu() {
        mainFrame.editMenu.add(actionRegistry[UNDO_ACTION])
        mainFrame.editMenu.add(actionRegistry[REDO_ACTION])
    }

    private fun buildObjectMenu() {
        mainFrame.objectMenu.add(actionRegistry[EDIT_LABEL_ACTION])
        mainFrame.objectMenu.add(JCheckBoxMenuItem(actionRegistry[TOGGLE_ATOM_VISIBILITY_ACTION]))
        mainFrame.objectMenu.add(actionRegistry[DELETE_ATOM_MENU_ACTION])
        mainFrame.objectMenu.add(JCheckBoxMenuItem(actionRegistry[IGNORE_VALENCY_ERRORS_ACTION]))
        mainFrame.objectMenu.addSeparator()
        mainFrame.objectMenu.add(actionRegistry[FLIP_SELECTED_ACTION])
        mainFrame.objectMenu.add(JCheckBoxMenuItem(actionRegistry[SET_SINGLE_BOND_ACTION]))
        mainFrame.objectMenu.add(JCheckBoxMenuItem(actionRegistry[SET_WEDGED_ACTION]))
        mainFrame.objectMenu.add(JCheckBoxMenuItem(actionRegistry[SET_HASHED_ACTION]))
        mainFrame.objectMenu.add(actionRegistry[FLIP_STEREO_CHEM_ACTION])
        mainFrame.objectMenu.add(JCheckBoxMenuItem(actionRegistry[SET_DOUBLE_BOND_ACTION]))
        mainFrame.objectMenu.add(JCheckBoxMenuItem(actionRegistry[SET_AROMATIC_ACTION]))
        mainFrame.objectMenu.add(JCheckBoxMenuItem(actionRegistry[SET_TRIPLE_BOND_ACTION]))
        mainFrame.objectMenu.add(actionRegistry[DELETE_BOND_ACTION])
        mainFrame.objectMenu.addSeparator()
        mainFrame.objectMenu.add(actionRegistry[COPY_SELECTION_ACTION])
        mainFrame.objectMenu.add(actionRegistry[PASTE_SELECTION_ACTION])
        mainFrame.objectMenu.add(actionRegistry[CUT_SELECTION_ACTION])
        mainFrame.objectMenu.add(actionRegistry[DELETE_SELECTION_ACTION])
        mainFrame.objectMenu.addSeparator()
        mainFrame.objectMenu.add(actionRegistry[CDK_COPY_CANONICAL_SMILES_ACTION])
        mainFrame.objectMenu.add(actionRegistry[CDK_CLEANUP_STRUCTURE])
    }

    private fun buildAboutMenu() {
        mainFrame.helpMenu.add(actionRegistry[VISIT_WEBSITE_ACTION])
        mainFrame.helpMenu.add(actionRegistry[VISIT_REPO_ACTION])
        mainFrame.helpMenu.add(actionRegistry[VISIT_ISSUE_TRACKER_ACTION])
        mainFrame.helpMenu.add(actionRegistry[VISIT_ABOUT_ACTION])
    }

    override fun getActionRegistry(): SwingActionRegistry {
        return actionRegistry
    }

    companion object {
        const val NEW_PROJECT_ACTION = "NEW_PROJECT"
        const val LOAD_PROJECT_ACTION = "LOAD_PROJECT"
        const val SAVE_PROJECT_ACTION = "SAVE_PROJECT"
        const val SAVE_PROJECT_AS_ACTION = "SAVE_PROJECT_AS"
        const val QUIT_APPLICATION_ACTION = "QUIT_APP"
        const val UNDO_ACTION = "UNDO"
        const val REDO_ACTION = "REDO"
        const val VISIT_WEBSITE_ACTION = "VISIT_WEBSITE"
        const val VISIT_REPO_ACTION = "VISIT_REPO"
        const val VISIT_ISSUE_TRACKER_ACTION = "VISIT_ISSUE_TRACKER"
        const val VISIT_ABOUT_ACTION = "VISIT_ABOUT"

        const val EDIT_LABEL_ACTION = "EDIT_LABEL_ACTION"
        const val DELETE_ATOM_MENU_ACTION = "DELETE_ATOM"
        const val TOGGLE_ATOM_VISIBILITY_ACTION = "TOGGLE_ATOM_VISIBILITY"
        const val IGNORE_VALENCY_ERRORS_ACTION = "IGNORE_VALENCY_ERRORS"
        const val FLIP_SELECTED_ACTION = "FLIP_SELECTED"
        const val SET_SINGLE_BOND_ACTION = "SET_SINGLE"
        const val SET_HASHED_ACTION = "SET_HASHED"
        const val SET_WEDGED_ACTION = "SET_WEDGED"
        const val FLIP_STEREO_CHEM_ACTION = "FLIP_STEREO"
        const val SET_DOUBLE_BOND_ACTION = "SET_DOUBLE"
        const val SET_AROMATIC_ACTION = "SET_AROMATIC"
        const val SET_TRIPLE_BOND_ACTION = "SET_TRIPLE"
        const val DELETE_BOND_ACTION = "DELETE_BOND"
        const val COPY_SELECTION_ACTION = "COPY_SELECTION"
        const val PASTE_SELECTION_ACTION = "PASTE_SELECTION"
        const val CUT_SELECTION_ACTION = "CUT_SELECTION"
        const val DELETE_SELECTION_ACTION = "DELETE_SELECTION"

        const val CDK_COPY_CANONICAL_SMILES_ACTION = "COPY_CANONICAL"
        const val CDK_CLEANUP_STRUCTURE = "CDK_CLEANUP"
    }
}