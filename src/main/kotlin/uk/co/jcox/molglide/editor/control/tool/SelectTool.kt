package uk.co.jcox.molglide.editor.control.tool

import uk.co.jcox.molglide.editor.control.ActionManager
import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.SelectionManager
import uk.co.jcox.molglide.editor.control.actions.CompoundAction
import uk.co.jcox.molglide.editor.control.actions.IDataAction
import uk.co.jcox.molglide.editor.control.actions.MoveAtomAction
import javax.vecmath.Point2d

/**
 * This tool works by interacting with the Selection Manager's axis aligned bounding selection box
 *
 * 1) The editor panel updates the current selection box through the controller
 *
 * 2) This tool intercepts any click actions
 *
 * 3) If a click (and then a subsequent mouse drag action) takes place when the mouse is
 * selected over an atom/element that was already selected by the AABB, then the user will be allowed
 * to drag the atoms to a new position.
 *
 * Since dragging an atom to a new position is a continuous "dynamic" action, and not something that can be easily
 * described through a simple action do, and action undo, a fake action has to be submitted to the action manager.
 *
 * This pseudo action stores the original position, and then the position after the mouse is released.
 *
 * 4) If a click happens when the mouse is selected over something that was not covered by the AABB
 * then deselect everything and continue on as before!
 */



class SelectTool(actionManager: ActionManager, selectionManager: SelectionManager, editorData: EditorStateData) : Tool(actionManager, selectionManager) {

    private var toolMode: ToolMode = ToolMode.None

    override fun onClick(clickX: Int, clickY: Int) {
        toolMode = getToolMode(clickX, clickY)

        if (toolMode == ToolMode.None) {
            selectionManager.clearSelectionBoundingBox()
            return
        }
    }

    override fun onRelease(clickX: Int, clickY: Int) {
        //On release we have to make the fake action
        val m = toolMode
        when (m) {
            is ToolMode.Dragging -> submitActions(m)
            is ToolMode.None -> {}
        }
        toolMode = ToolMode.None
    }

    private fun submitActions(m: ToolMode.Dragging) {
        val actionList = mutableListOf<IDataAction>()
        m.posMap.forEach { (chemAtom, originalPos) ->
            val currentPos = chemAtom.atom.point2d
            val pseudoAction = MoveAtomAction(chemAtom, currentPos, originalPos)
            actionList.add(pseudoAction)
        }
        val actions = actionList.toTypedArray()
        val compoundAction = CompoundAction(*actions)
        actionManager.executeAction(compoundAction)
    }

    override fun onDragMouse(clickX: Int, clickY: Int, dx: Double, dy: Double) {
        val m = toolMode

        when (m) {
            is ToolMode.Dragging -> handleSelectionDrag(m, clickX, clickY)
            is ToolMode.None -> {}
        }

    }


    private fun handleSelectionDrag(m: ToolMode.Dragging, clickX: Int, clickY: Int) {
        //The user is dragging on an already selected item
        //or a group of items.
        selectionManager.batchSelection.atoms.forEach { chemAtom ->
            if (!m.posMap.containsKey(chemAtom)) {
                m.posMap[chemAtom] = Point2d(chemAtom.atom.point2d.x, chemAtom.atom.point2d.y)
            }

            val originalPos = m.posMap[chemAtom] ?: return

            val newPosX = originalPos.x + (clickX.toDouble() - m.firstClickX)
            val newPosY = originalPos.y + (clickY.toDouble() - m.firstClickY)

            chemAtom.atom.point2d.x = newPosX
            chemAtom.atom.point2d.y = newPosY
        }
    }

    override fun onSuddenMove() {

    }


    private fun getToolMode(clickX: Int, clickY: Int) : ToolMode {
        //Check if the selected atom was covered in the AABB
        val batchSelection = selectionManager.batchSelection
        val mouseOverSelection = selectionManager.primarySelection.inPrimarySelection(batchSelection)
        if (mouseOverSelection) {
            return ToolMode.Dragging(clickX, clickY)
        }
        return ToolMode.None
    }

    fun shouldShowAABB(): Boolean {
        return toolMode == ToolMode.None
    }



    sealed class ToolMode {
        object None: ToolMode()
        class Dragging(val firstClickX: Int, val firstClickY: Int, val posMap: MutableMap<ChemMolecule.ChemAtom, Point2d> = mutableMapOf()) : ToolMode()
    }
}