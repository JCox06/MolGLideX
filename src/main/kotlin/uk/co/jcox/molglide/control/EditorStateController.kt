package uk.co.jcox.molglide.control

import org.joml.Vector2d
import org.xmlcml.euclid.Vector2
import uk.co.jcox.molglide.EditMode
import uk.co.jcox.molglide.control.tool.AtomBondTool
import uk.co.jcox.molglide.control.tool.Tool
import uk.co.jcox.molglide.ui.EditorPanel

class EditorStateController (
    private val appManager: AppManager,
    private val stateData : EditorStateData,
) {
    val actionManager: ActionManager = ActionManager(stateData)

    private val selectionManager: SelectionManager = SelectionManager()
    private var currentTool: Tool = AtomBondTool(appManager, actionManager, selectionManager, stateData)

    val uiBuilder = UIBuilder(stateData)


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

    fun getSelectedFormula(): String {
        val s = selectionManager.primarySelection
        if (s is SelectionManager.Type.ActiveAtom) {
            return s.chemAtom.molecule.getFormulaString()
        }
        return ""
    }

    fun isAtomSelected() : Boolean {
        val selection = selectionManager.primarySelection
        if (selection is SelectionManager.Type.ActiveAtom) {
            return true
        }
        return false
    }

    fun isBondSelected(): Boolean {
        val selection = selectionManager.primarySelection
        if (selection is SelectionManager.Type.ActiveBond) {
            return true
        }
        return false
    }

    fun getSelectedWeight(): Double {
        val s = selectionManager.primarySelection
        if (s is SelectionManager.Type.ActiveAtom) {
            return s.chemAtom.molecule.getMolecularWeight()
        }
        return 0.0
    }

    fun getSelectedBondPos(): Vector2d? {
        val selection = selectionManager.primarySelection
        if (selection is SelectionManager.Type.ActiveBond) {
            return selection.chemBond.midPoint()
        }
        return null
    }
}