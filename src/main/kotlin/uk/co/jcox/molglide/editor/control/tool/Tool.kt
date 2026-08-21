package uk.co.jcox.molglide.editor.control.tool

import uk.co.jcox.molglide.editor.control.ActionManager
import uk.co.jcox.molglide.editor.control.EventContext
import uk.co.jcox.molglide.editor.model.SelectionManager

abstract class Tool (
    protected val actionManager: ActionManager,
    protected val selectionManager: SelectionManager,
) {
    protected var mouseX: Int = 0
    protected var mouseY: Int = 0


    //General tool events that
    abstract fun onClick(clickX: Int, clickY: Int, eventContext: EventContext)
    abstract fun onRelease(clickX: Int, clickY: Int, eventContext: EventContext)
    abstract fun onDragMouse(clickX: Int, clickY: Int, dx: Double, dy: Double, eventContext: EventContext)
    abstract fun onSuddenMove()


    /**
     * Indicates to the renderer whether the selection
     * box can be shown upon the user dragging the mouse
     */
    open fun shouldShowAABB(): Boolean {
        return false
    }


    fun updateMouseWorld(mouseX: Int, mouseY: Int) {
        this.mouseX = mouseX
        this.mouseY = mouseY
    }
}