package uk.co.jcox.molglide.editor.control.tool

import org.apache.jena.sparql.function.library.date
import org.joml.Vector2d
import org.xmlcml.euclid.Vector2
import uk.co.jcox.molglide.editor.control.ActionManager
import uk.co.jcox.molglide.editor.control.EventContext
import uk.co.jcox.molglide.editor.control.actions.RegisterChemArrowAction
import uk.co.jcox.molglide.editor.model.ChemArrow
import uk.co.jcox.molglide.editor.model.EditorStateData
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
            val action = RegisterChemArrowAction(m.newArrow)
            m.newArrow.setTransient(false)
            actionManager.executeAction(action)
        }
        toolMode = ToolMode.None
    }

    override fun onDragMouse(clickX: Int, clickY: Int, dx: Double, dy: Double, eventContext: EventContext) {
        val m = toolMode
        if (m is ToolMode.NewArrowDragging) {
            m.newArrow.end.x = clickX.toDouble()
            m.newArrow.end.y = clickY.toDouble()
        }
    }

    override fun onSuddenMove() {

    }


    private fun getToolMode(clickX: Int, clickY: Int) : ToolMode {
        if (selectionManager.primarySelection == null) {
            val newArrow = ChemArrow(Vector2d(clickX.toDouble(), clickY.toDouble()), Vector2d(clickX.toDouble(), clickY.toDouble()), null)
            data.addArrow(newArrow)
            newArrow.setTransient(true)
            return ToolMode.NewArrowDragging(newArrow)
        }
        return ToolMode.None
    }


    sealed class ToolMode {
        object None: ToolMode()
        class NewArrowDragging(val newArrow: ChemArrow) : ToolMode()
    }
}