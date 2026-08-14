package uk.co.jcox.molglide.control

import com.sun.org.apache.xpath.internal.operations.Bool
import uk.co.jcox.molglide.EditMode
import uk.co.jcox.molglide.StereoChem
import uk.co.jcox.molglide.control.actions.AtomDeletionAction
import uk.co.jcox.molglide.control.actions.BondDeletionAction
import uk.co.jcox.molglide.control.actions.ChangeStereoChemAction
import uk.co.jcox.molglide.control.actions.ToggleAtomVisibilityAction
import uk.co.jcox.molglide.control.tool.AtomBondTool
import uk.co.jcox.molglide.control.tool.Tool
import uk.co.jcox.molglide.ui.DeleteBondMenuAction
import uk.co.jcox.molglide.ui.EditorPanel

class EditorStateController (
    private val appManager: AppManager,
    private val stateData : EditorStateData,
) {
    val actionManager: ActionManager = ActionManager(stateData)

    private val selectionManager: SelectionManager = SelectionManager()
    private var currentTool: Tool = AtomBondTool(appManager, actionManager, selectionManager, stateData)

    val uiBuilder = UIBuilder(stateData, selectionManager)


    fun handleMouseClick(mouseX: Int, mouseY: Int) {
        prepareTool()
        currentTool.onClick(mouseX, mouseY)
        uiBuilder.rebuild()
    }

    fun handleMouseRelease(mouseX: Int, mouseY: Int) {
        currentTool.onRelease(mouseX, mouseY)
        uiBuilder.rebuild()
    }

    fun handleSuddenMouseMove() {
        currentTool.onSuddenMove()
    }

    fun handleMouseDrag(mouseX: Int, mouseY: Int) {
        currentTool.onDragMouse(mouseX, mouseY)
    }

    private fun prepareTool() {
        if (appManager.editMode.type == EditMode.ToolType.ATOM_INSERT) {
            currentTool = AtomBondTool(appManager, actionManager, selectionManager, stateData)
        }
    }

    fun update(worldX: Int, worldY: Int) {
        currentTool.updateMouseWorld(worldX, worldY)
        currentTool.runUpdates()
        selectionManager.update(stateData, worldX, worldY)
        if (actionManager.isDirty) {
            uiBuilder.rebuild()
        }
    }

    fun checkSelected(atom: UIAtom) : Boolean {
        val selection = selectionManager.primarySelection
        return selection is SelectionManager.Type.ActiveAtom && selection.chemAtom.atom.id == atom.chemID
    }

    fun nowActive(panel: EditorPanel) {
        appManager.activeTab = this
        appManager.activePanel = panel
    }

    fun deleteSelectedAtom() {
        val atom = selectionManager.getAtom() ?: return
        val action = AtomDeletionAction(atom)
        actionManager.executeAction(action)
    }

    fun toggleSelectedAtomVisiblity() {
        val atom = selectionManager.getAtom() ?: return
        val action = ToggleAtomVisibilityAction(atom)
        actionManager.executeAction(action)

    }

    fun flipSelectedBond() {
        TODO()
    }

    fun updateSingleSelectedBond(bondOptions: StereoChem) {
        val bond = selectionManager.getBond() ?: return
        val action = ChangeStereoChemAction(bond, bondOptions)
        actionManager.executeAction(action)
    }

    fun updateDoubleSelectedBond() {
        val bond = selectionManager.getBond() ?: return
        TODO()
    }

    fun updateAromaticSelectedBond() {
        TODO()
    }

    fun invertStereoChemSelectedBond() {
        TODO()
    }

    fun setTripleSelectedBond() {
        TODO()
    }

    fun deleteSelectedBond() {
        val selection = selectionManager.primarySelection
        if (selection is SelectionManager.Type.ActiveBond) {
            val action = BondDeletionAction(selection.chemBond)
            actionManager.executeAction(action)
        }
    }
}