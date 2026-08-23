package uk.co.jcox.molglide.editor.model

import org.joml.Vector2d

interface IEditorSelectable {


    /**
     * This is what the selection manager uses to figure out what is selected
     * 1) Multiple objects may or may not have multiple selection points
     * - In the case of arrows, there is a start, end, and middle point for the Bézier curve
     * - This means the line has three selection points
     * 2) The selection manager receives a list of points
     * 3) The Arrow tool knows which point is which based on the index that the mouse is near
     */
    fun getObjectSelectionPoints(): Map<Int, Vector2d>

}