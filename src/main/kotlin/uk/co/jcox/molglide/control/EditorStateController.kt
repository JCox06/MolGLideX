package uk.co.jcox.molglide.control

import uk.co.jcox.molglide.EditMode
import uk.co.jcox.molglide.control.tool.AtomBondTool
import uk.co.jcox.molglide.control.tool.Tool

class EditorStateController (
    private val appManager: AppManager,
    private val stateData : EditorStateData,
) {
    val actionManager: ActionManager = ActionManager(stateData)

    private val selectionManager: SelectionManager = SelectionManager()
    private var currentTool: Tool = AtomBondTool(appManager, actionManager, selectionManager, stateData)

    private val uiAtoms: MutableList<UIAtom> = mutableListOf()
    private val uiBonds: MutableList<UIBond> = mutableListOf()

    fun getUIAtoms(): List<UIAtom> {
        return uiAtoms
    }

    fun getBondsToDraw() : List<UIBond> {
        return uiBonds
    }

    fun handleMouseClick(mouseX: Int, mouseY: Int) {
        prepareTool()
        currentTool.onClick(mouseX, mouseY)
        rebuildUI()
    }

    fun handleMouseRelease(mouseX: Int, mouseY: Int) {
        currentTool.onRelease(mouseX, mouseY)
        rebuildUI()
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
            rebuildUI()
        }
    }

    private fun rebuildUI() {
        uiAtoms.clear()
        uiBonds.clear()
        stateData.getMolecules().forEach { molecule ->
            uiAtoms.addAll(molecule.getUIProperties())
            uiBonds.addAll(molecule.getUIBondProperties())
        }
    }

    fun checkSelected(atom: UIAtom) : Boolean {
        val selection = selectionManager.primary
        return selection is SelectionManager.Type.Active && selection.chemAtom.atom.id == atom.id
    }

    fun nowActive() {
        appManager.activeTab = this
    }

    fun getSelectedFormula(): String {
        val s = selectionManager.primary
        if (s is SelectionManager.Type.Active) {
            return s.chemAtom.molecule.getFormulaString()
        }
        return ""
    }

    fun getSelectedWeight(): Double {
        val s = selectionManager.primary
        if (s is SelectionManager.Type.Active) {
            return s.chemAtom.molecule.getMolecularWeight()
        }
        return 0.0
    }
}