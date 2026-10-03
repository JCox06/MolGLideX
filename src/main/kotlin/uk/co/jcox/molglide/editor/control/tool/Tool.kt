package uk.co.jcox.molglide.editor.control.tool

import uk.co.jcox.molglide.editor.control.ActionManager
import uk.co.jcox.molglide.editor.control.EventContext
import uk.co.jcox.molglide.editor.model.SelectionManager

/**
 * A tool represents the current visual mode the editor is in.
 * A tool is primarily used to capture the mouse position, and mouse events.
 *
 * Keyboard events (and context popup menus) are not normalised and are just handled directly in the controller
 */
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

    /**
     * If the mouse moves suddenly this method is called
     * Suddenly is defined in editor constants
     */
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

    /**
     * Checks if a given object can be selected as the primary selection when this tool is active
     * It can also be used to selectively choose what object anchors to allow to be selected
     *
     * For instance, the ChemArrow tool might just want to allow the user to select arrows and not atoms!
     * In this case, if the Selectable is a ChemArrow then return true, else false.
     */
    open fun canAccept(selectionContext: SelectionManager.SelectionInfo): Boolean {
        return true
    }
}