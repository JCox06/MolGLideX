package uk.co.jcox.molglide.editor.control.tool

import uk.co.jcox.molglide.editor.control.ActionManager
import uk.co.jcox.molglide.editor.model.SelectionManager

abstract class Tool (
    protected val actionManager: ActionManager,
    protected val selectionManager: SelectionManager,
) {
    protected var mouseX: Int = 0
    protected var mouseY: Int = 0

    abstract fun onClick(clickX: Int, clickY: Int)
    abstract fun onRelease(clickX: Int, clickY: Int)
    abstract fun onDragMouse(clickX: Int, clickY: Int, dx: Double, dy: Double)
    abstract fun onSuddenMove()

    fun updateMouseWorld(mouseX: Int, mouseY: Int) {
        this.mouseX = mouseX
        this.mouseY = mouseY
    }
}