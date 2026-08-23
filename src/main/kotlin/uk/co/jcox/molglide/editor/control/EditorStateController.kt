package uk.co.jcox.molglide.editor.control

import com.github.jsonldjava.shaded.com.google.common.math.IntMath.pow
import org.openscience.cdk.interfaces.IBond
import uk.co.jcox.molglide.EditMode
import uk.co.jcox.molglide.IEditorSessionOrganiser
import uk.co.jcox.molglide.editor.control.tool.AtomBondTool
import uk.co.jcox.molglide.editor.control.tool.FormalChargeLonePairTool
import uk.co.jcox.molglide.editor.control.tool.SelectTool
import uk.co.jcox.molglide.editor.control.tool.TemplateRingTool
import uk.co.jcox.molglide.editor.control.tool.Tool
import uk.co.jcox.molglide.IMainAppData
import uk.co.jcox.molglide.StereoChem
import uk.co.jcox.molglide.editor.control.actions.AtomDeletionAction
import uk.co.jcox.molglide.editor.control.actions.BondDeletionAction
import uk.co.jcox.molglide.editor.control.actions.ChangeStereoChemAction
import uk.co.jcox.molglide.editor.control.actions.CleanupStructure
import uk.co.jcox.molglide.editor.control.actions.CompoundAction
import uk.co.jcox.molglide.editor.control.actions.FlipBondAction
import uk.co.jcox.molglide.editor.control.actions.IDataAction
import uk.co.jcox.molglide.editor.control.actions.ImportMoleculesAction
import uk.co.jcox.molglide.editor.control.actions.PartitionFragmentsAction
import uk.co.jcox.molglide.editor.control.actions.ReplaceAtomAction
import uk.co.jcox.molglide.editor.control.actions.SetIgnoreErrorsOnAtom
import uk.co.jcox.molglide.editor.control.actions.ToggleAtomVisibilityAction
import uk.co.jcox.molglide.editor.control.actions.TranslateAtomAction
import uk.co.jcox.molglide.editor.control.actions.UpdateBondAromaticityAction
import uk.co.jcox.molglide.editor.control.actions.UpdateBondOrderAction
import uk.co.jcox.molglide.editor.control.tool.ArrowTool
import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.ui.EditorPanel
import uk.co.jcox.molglide.editor.ui.EditorPanel.Companion.MOUSE_SENSE
import uk.co.jcox.molglide.editor.ui.EditorPanel.Companion.MOUSE_SENSE_ZOOM
import uk.co.jcox.molglide.editor.ui.EditorPanel.Companion.SIG_MOUSE_DELTA
import java.awt.Point
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import javax.swing.SwingUtilities
import javax.swing.Timer
import javax.swing.event.PopupMenuEvent
import javax.swing.event.PopupMenuListener
import kotlin.collections.toTypedArray
import kotlin.math.sqrt

class EditorStateController (
    private val globalContext: IMainAppData,
    private val stateData : EditorStateData,
    private val editorPanel: EditorPanel,
    private val sessionOrganiser: IEditorSessionOrganiser,
) {
    val actionManager: ActionManager = ActionManager(stateData) { dataHasChanged() }
    private var currentTool: Tool = AtomBondTool(globalContext, actionManager, stateData.selectionManager, stateData)

    init {
        val panelMouseEvents = PanelMouseEvents()
        editorPanel.addMouseMotionListener(panelMouseEvents)
        editorPanel.addMouseListener(panelMouseEvents)
        editorPanel.addMouseWheelListener(panelMouseEvents)


        val timer = Timer(16) {
            editorPanel.refreshEditor()
            val world = screenToWorld(stateData.mouseX.toDouble(), stateData.mouseY.toDouble())
            update(world.x, world.y)

            stateData.uiDataBuilder.rebuild(false)
        }
        timer.start()

        //First time run immediately do a full UI rebuild
        stateData.uiDataBuilder.rebuild(true)

        editorPanel.buildContextMenus(sessionOrganiser.getActionRegistry(), MenuPopupListener())
    }

    private fun handleMouseClick(clickX: Int, clickY: Int, eventContext: EventContext) {
        currentTool.onClick(clickX, clickY, eventContext)
    }

    private fun handleMouseRelease(clickX: Int, clickY: Int, eventContext: EventContext) {
        currentTool.onRelease(clickX, clickY, eventContext)
    }

    private fun handleSuddenMouseMove() {
        currentTool.onSuddenMove()
    }

    private fun handleMouseDrag(mouseX: Int, mouseY: Int, dx: Double, dy: Double, eventContext: EventContext) {
        currentTool.onDragMouse(mouseX, mouseY, dx, dy, eventContext)
    }

    private fun translateCameraPos(x: Double, y: Double) {
        stateData.cameraX += x
        stateData.cameraY += y
    }

    fun update(worldX: Int, worldY: Int) {
        currentTool.updateMouseWorld(worldX, worldY)
        stateData.selectionManager.updatePrimarySelection(stateData, worldX, worldY, currentTool)
    }

    fun prepareTool() {
        if (globalContext.getEditMode().type == EditMode.ToolType.ATOM_INSERT) {
            currentTool = AtomBondTool(globalContext, actionManager, stateData.selectionManager, stateData)
            stateData.selectionManager.clearSelectionBoundingBox()
        }
        if (globalContext.getEditMode().type == EditMode.ToolType.RING_INSERT) {
            currentTool = TemplateRingTool(globalContext, actionManager, stateData.selectionManager)
            stateData.selectionManager.clearSelectionBoundingBox()
        }
        if (globalContext.getEditMode().type == EditMode.ToolType.SELECT_TOOL && currentTool !is SelectTool) {
            currentTool = SelectTool(actionManager, stateData.selectionManager, stateData)
        }

        if (globalContext.getEditMode().type == EditMode.ToolType.FORMAL_CHARGE) {
            currentTool = FormalChargeLonePairTool(globalContext, actionManager, stateData.selectionManager)
        }

        if (globalContext.getEditMode().type == EditMode.ToolType.ARROW_CREATOR) {
            currentTool = ArrowTool(stateData, actionManager, stateData.selectionManager)
        }
    }

    private fun screenToWorld(screen: Point): Point {
        val x: Double = screen.x.toDouble()
        val y: Double = screen.y.toDouble()
        return screenToWorld(x, y)
    }

    private fun screenToWorld(screenX: Double, screenY: Double) : Point {
        var x = screenX
        var y = screenY
        val camX = stateData.cameraX
        val camY = stateData.cameraY
        val camZoom = stateData.cameraZoom
        x -= camX
        y -= camY
        x /= camZoom
        y /= camZoom
        return Point(x.toInt(), y.toInt())
    }

    //Rebuilds the entire UI
    private fun dataHasChanged() {
        rebuildEntireUI()
        sessionOrganiser.onDocumentDirty(stateData.sessionID)
    }

    private fun rebuildEntireUI() {
        stateData.uiDataBuilder.rebuild(true)
    }

    fun updateAtomLabel(newSymbol: String) {
        val atom = stateData.selectionManager.getAtom() ?: return
        val replaceAtomAction = ReplaceAtomAction(atom, newSymbol)
        actionManager.executeAction(replaceAtomAction)
    }

    fun deleteSelectedAtom() {
        val atom = stateData.selectionManager.getAtom() ?: return
        val deleteAtom = AtomDeletionAction(atom)
        val frag = PartitionFragmentsAction(atom.molecule)
        actionManager.executeAction(CompoundAction(deleteAtom, frag))
    }

    fun toggleSelectedAtomVisibility() {
        val atom = stateData.selectionManager.getAtom() ?: return
        val action = ToggleAtomVisibilityAction(atom)
        actionManager.executeAction(action)

    }
    fun flipSelectedBond() {
        val bond = stateData.selectionManager.getBond() ?: return
        val action = FlipBondAction(bond)
        actionManager.executeAction(action)
    }

    fun updateSingleSelectedBond(bondOptions: StereoChem) {
        val bond = stateData.selectionManager.getBond() ?: return

        //Again as explained below, I am only allowing single bonds to have stereochem
        val changeOrder = UpdateBondOrderAction(bond, IBond.Order.SINGLE)
        val changeStereo = ChangeStereoChemAction(bond, bondOptions)
        val compoundAction = CompoundAction(changeOrder, changeStereo)
        actionManager.executeAction(compoundAction)
    }

    fun invertStereoChemSelectedBond() {
        TODO()
    }

    fun updateDoubleSelectedBond() {
        val bond = stateData.selectionManager.getBond() ?: return
        //Although double bonds do have stereochemistry
        //it is not really depicted in chemical sketchers
        //So I think for a simple molecular editor, its okay for the moment to assume
        //double bonds should NOT have stereochemistry information associated with them
        val removeStereoAction = ChangeStereoChemAction(bond, StereoChem.NORMAL)
        val updateBondOrderAction = UpdateBondOrderAction(bond, IBond.Order.DOUBLE)
        val compoundAction = CompoundAction(removeStereoAction, updateBondOrderAction)
        actionManager.executeAction(compoundAction)
    }

    //Like the methods above, it might look first strange to see that this is not a compound action
    //that affects the double bond option too.
    //However in the case of benzene, the CDK recommends to have all the bonds (including formally single bonds)
    //to be set as aromatic
    fun updateAromaticSelectedBond() {
        val bond = stateData.selectionManager.getBond() ?: return
        val action = UpdateBondAromaticityAction(bond)
        actionManager.executeAction(action)
    }

    fun setTripleSelectedBond() {
        val bond = stateData.selectionManager.getBond() ?: return
        val action = UpdateBondOrderAction(bond, IBond.Order.TRIPLE)
        actionManager.executeAction(action)
    }


    fun deleteSelectedBond() {
        val bond = stateData.selectionManager.getBond() ?: return
        val bondDelete = BondDeletionAction(bond)
        val fragment = PartitionFragmentsAction(bond.molecule)
        actionManager.executeAction(CompoundAction(bondDelete, fragment))

    }

    fun ignoreErrors() {
        val chemAtom = stateData.selectionManager.getAtom() ?: return
        val action = SetIgnoreErrorsOnAtom(chemAtom, !chemAtom.shouldIgnoreErrors())
        actionManager.executeAction(action)
    }

    fun importLevel(importData: EditorStateData, newMouseX: Int, newMouseY: Int, oldMouseX: Int, oldMouseY: Int) {

        //The meta data contains the mouse click on copy
        //Compare this with the current screen x, and y, to get dy, and dx, for the copy

        val mouseWorld = screenToWorld(newMouseX.toDouble(), newMouseY.toDouble())

        val importActionManager = ActionManager(importData)
        val rel = importData.getMolecules().first().atoms().first().getPos()
        importData.getMolecules().forEach { chemMolecule ->
            chemMolecule.atoms().forEach { chemAtom ->
                //First we need to move the atom to a new position based on where the mouse clicked
                //A simple move action would move all the atoms to the same position
                //So we have to calculate the dx, dy for each individual atom as to
                //preserve the original structure
                val dx = mouseWorld.x - rel.x
                val dy = mouseWorld.y - rel.y
                val moveAtom = TranslateAtomAction(chemAtom, dx, dy)
                importActionManager.executeAction(moveAtom)
            }
            //Then validate the data in the level and ensure it is all properly
            //fragmented
            val partitionAction = PartitionFragmentsAction(chemMolecule)
            importActionManager.executeAction(partitionAction)
        }
        //Now import the data
        val importAction = ImportMoleculesAction(importData)
        this.actionManager.executeAction(importAction)

        //Now remove the old selection, and highlight the newly selected data
        this.stateData.selectionManager.clearAndAddSelection(importData.getMolecules())
    }

    fun deleteSelectedComponents() {

        val molsToCheck = mutableSetOf<ChemMolecule>()

        val actions: MutableList<IDataAction> = mutableListOf()

        //Note: Remove bonds first, and then atoms
        //On the reverse when the user wants to undo the action, CompoundAction reverses
        //the order of the actionArray, so the atoms must be present first
        stateData.selectionManager.getBatchBonds().forEach { chemBond ->
            val deleteBondAction = BondDeletionAction(chemBond)
            actions.add(deleteBondAction)
            molsToCheck.add(chemBond.molecule)
        }
        stateData.selectionManager.getBatchAtoms().forEach { chemAtom ->
            val deleteAtomAction = AtomDeletionAction(chemAtom)
            actions.add(deleteAtomAction)
            molsToCheck.add(chemAtom.molecule)
        }

        molsToCheck.forEach { molecule ->
            val partition = PartitionFragmentsAction(molecule)
            actions.add(partition)
        }

        val actionArray = actions.toTypedArray()

        val compoundAction = CompoundAction(*actionArray)
        actionManager.executeAction(compoundAction)

        stateData.selectionManager.clearSelectionBoundingBox()
    }


    fun cleanUpSelectedMolecule() {
        val molecule = stateData.selectionManager.getMolecule() ?: return
        val action = CleanupStructure(molecule)
        actionManager.executeAction(action)
    }


    inner class MenuPopupListener: PopupMenuListener {
        override fun popupMenuWillBecomeVisible(e: PopupMenuEvent?) {
            stateData.pauseEvents = true
        }

        override fun popupMenuWillBecomeInvisible(e: PopupMenuEvent?) {
            stateData.pauseEvents = false
        }

        override fun popupMenuCanceled(e: PopupMenuEvent?) {
            stateData.pauseEvents = false
        }

    }

    inner class PanelMouseEvents: MouseAdapter() {

        private var offsetX: Int = 0
        private var offsetY: Int = 0

        override fun mousePressed(e: MouseEvent?) {
            if (e == null || stateData.pauseEvents) {
                return
            }
            val world = screenToWorld(e.point)
            if (SwingUtilities.isLeftMouseButton(e)) {
                handleMouseClick(world.x, world.y, EventContext(e.isControlDown, e.isShiftDown))
            }
            stateData.transientBoxSelectStartX = world.x
            stateData.transientBoxSelectStartY = world.y

            maybeShowPopup(e)
        }

        override fun mouseReleased(e: MouseEvent?) {
            if (e == null || stateData.pauseEvents) {
                return
            }
            val point = screenToWorld(e.point)
            handleMouseRelease(point.x, point.y, EventContext(e.isControlDown, e.isShiftDown))

            stateData.transientBoxSelectStartX = 0
            stateData.transientBoxSelectStartY = 0
            stateData.transientBoxSelectAdvY = 0
            stateData.transientBoxSelectAdvX = 0

            maybeShowPopup(e)
        }

        override fun mouseWheelMoved(e: MouseWheelEvent?) {
            if (e == null || stateData.pauseEvents) {
                return
            }

            if (e.isControlDown) {
                stateData.cameraZoom -= e.wheelRotation * MOUSE_SENSE_ZOOM
            }
        }

        override fun mouseDragged(e: MouseEvent?) {
            if (e == null || stateData.pauseEvents) {
                return
            }

            updateMouse(e)

            val world = screenToWorld(e.point)
            val moveX = offsetX * MOUSE_SENSE
            val moveY = offsetY * MOUSE_SENSE

            if (SwingUtilities.isMiddleMouseButton(e)) {
                translateCameraPos(moveX, moveY)
                return
            }

            handleMouseDrag(world.x, world.y, moveX, moveY, EventContext(e.isShiftDown, e.isControlDown))


            if (stateData.canSelectBox && SwingUtilities.isLeftMouseButton(e) && currentTool.shouldShowAABB()) {
                stateData.transientBoxSelectAdvX = world.x - stateData.transientBoxSelectStartX
                stateData.transientBoxSelectAdvY = world.y - stateData.transientBoxSelectStartY
                stateData.selectionManager.updateSelectionBoundingBox(stateData, stateData.transientBoxSelectStartX, stateData.transientBoxSelectStartY, world.x, world.y)
            }
        }

        override fun mouseMoved(e: MouseEvent?) {
            if (e == null || stateData.pauseEvents) {
                return
            }
            updateMouse(e)
        }

        private fun updateMouse(e: MouseEvent) {
            if (stateData.pauseEvents) {
                return
            }
            val currentPos = e.point
            offsetX = currentPos.x - stateData.getLastMouseX()
            offsetY = currentPos.y - stateData.getLastMouseY()
            stateData.mouseX = currentPos.x
            stateData.mouseY = currentPos.y

            if (sqrt((pow(offsetX, 2) + pow(offsetY, 2)).toDouble()) >= SIG_MOUSE_DELTA) {
                handleSuddenMouseMove()
            }
        }

        private fun maybeShowPopup(e: MouseEvent) {
            if (!e.isPopupTrigger || !SwingUtilities.isRightMouseButton(e)) {
                return
            }

            if (stateData.selectionManager.hasBatchSelection()) {
                editorPanel.selectionMenu?.show(e.component, e.x, e.y)
                return
            }

            if (stateData.selectionManager.getAtom() != null) {
                editorPanel.atomMenu?.show(e.component, e.x, e.y)
                return
            }

            if (stateData.selectionManager.getBond() != null) {
                editorPanel.bondMenu?.show(e.component, e.x, e.y)
                return
            }
            editorPanel.normalMenu?.show(e.component, e.x, e.y)
        }
    }
}