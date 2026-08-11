package uk.co.jcox.molglide.control.tool

import uk.co.jcox.molglide.control.ActionManager
import uk.co.jcox.molglide.control.EditorStateData
import uk.co.jcox.molglide.control.SelectionManager

abstract class Tool (
    private val actionManager: ActionManager,
    private val selectionManager: SelectionManager,
    private val editorData: EditorStateData
) {
    protected var mouseX: Int = 0
    protected var mouseY: Int = 0

    abstract fun onClick(clickX: Int, clickY: Int)
    abstract fun onRelease(clickX: Int, clickY: Int)
    abstract fun onDragMouse(clickX: Int, clickY: Int)
    abstract fun onSuddenMove()
    abstract fun runUpdates()

    fun updateMouseWorld(mouseX: Int, mouseY: Int) {
        this.mouseX = mouseX
        this.mouseY = mouseY
    }
}