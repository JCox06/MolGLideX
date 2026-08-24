package uk.co.jcox.molglide.editor.control.tool

import org.joml.Vector2d
import uk.co.jcox.molglide.editor.control.ActionManager
import uk.co.jcox.molglide.editor.control.EventContext
import uk.co.jcox.molglide.editor.control.actions.RestoreChemArrowAction
import uk.co.jcox.molglide.editor.control.actions.RestoreChemArrowPositionAction
import uk.co.jcox.molglide.editor.model.ChemArrow
import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.IEditorSelectable
import uk.co.jcox.molglide.editor.model.SelectionManager

class ArrowTool(val data: EditorStateData, actionManager: ActionManager, selectionManager: SelectionManager) : Tool(actionManager,
    selectionManager
) {

    private var toolMode: ToolMode = ToolMode.None

    override fun onClick(clickX: Int, clickY: Int, eventContext: EventContext) {
        val newMode = getToolMode(clickX, clickY)
        toolMode = newMode
    }

    override fun onRelease(clickX: Int, clickY: Int, eventContext: EventContext) {
        val m = toolMode
        if (m is ToolMode.NewArrowDragging) {
            val action = RestoreChemArrowAction(m.newArrow)
            m.newArrow.setTransient(false)
            actionManager.executeAction(action)
        }
        if (m is ToolMode.ExistingArrowDragging) {
            val action = RestoreChemArrowPositionAction(m.chemArrow, m.existing, m.selection.objectAnchorID)
            actionManager.executeAction(action)
        }
        toolMode = ToolMode.None
    }

    override fun onDragMouse(clickX: Int, clickY: Int, dx: Double, dy: Double, eventContext: EventContext) {
        val m = toolMode
        if (m is ToolMode.NewArrowDragging) {
            dragNewArrow(m, clickX, clickY)
        }
        if (m is ToolMode.ExistingArrowDragging) {
            dragExisting(m, clickX, clickY)
        }
    }

    override fun onSuddenMove() {

    }


    private fun getToolMode(clickX: Int, clickY: Int) : ToolMode {

        val selection = selectionManager.primarySelection

        if (selection == null) {
            val newArrow = ChemArrow.ofSimple(Vector2d(clickX.toDouble(), clickY.toDouble()), Vector2d(clickX.toDouble(), clickY.toDouble()),
                Vector2d(clickX.toDouble(), clickY.toDouble()))
            data.addArrow(newArrow)
            newArrow.setTransient(true)
            return ToolMode.NewArrowDragging(newArrow)
        }

        val selectable = selection.selectable
        if (selectable is ChemArrow) {
            val pointToRestore = selectable.arrowPoints[selection.objectAnchorID] ?: Vector2d()
            return ToolMode.ExistingArrowDragging(selection, selectable, Vector2d(pointToRestore))
        }

        return ToolMode.None
    }


    private fun dragNewArrow(m: ToolMode.NewArrowDragging, clickX: Int, clickY: Int) {
        var currentX = clickX.toDouble()
        var currentY = clickY.toDouble()
        //Line snapping
        if (clickX.toDouble() in (m.newArrow.start().x - 5)..(m.newArrow.start().x +5)) {
            currentX = m.newArrow.start().x
        }
        if (clickY.toDouble() in (m.newArrow.start().y - 5)..(m.newArrow.start().y +5)) {
            currentY = m.newArrow.start().y
        }
        m.newArrow.end().x = currentX
        m.newArrow.end().y = currentY
        m.newArrow.controlA().x = (m.newArrow.start().x + m.newArrow.end().x) / 2
        m.newArrow.controlA().y = (m.newArrow.start().y + m.newArrow.end().y) /2
    }


    private fun dragExisting(m: ToolMode.ExistingArrowDragging, clickX: Int, clickY: Int) {
        val anchorID = m.selection.objectAnchorID
        val pointToMove = m.chemArrow.arrowPoints[anchorID]
        if (pointToMove != null) {
            pointToMove.x = clickX.toDouble()
            pointToMove.y = clickY.toDouble()
        }
    }

    override fun isTypeValidPrimarySelection(entity: IEditorSelectable): Boolean {
        return entity is ChemArrow
    }

    sealed class ToolMode {
        object None: ToolMode()
        class NewArrowDragging(val newArrow: ChemArrow) : ToolMode()
        class ExistingArrowDragging(val selection: SelectionManager.SelectionInfo, val chemArrow: ChemArrow, var existing: Vector2d) : ToolMode()
    }
}