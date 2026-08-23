package uk.co.jcox.molglide.editor.control.actions

import org.joml.Vector2d
import uk.co.jcox.molglide.editor.io.ArrowDataObject
import uk.co.jcox.molglide.editor.model.ChemArrow
import uk.co.jcox.molglide.editor.model.EditorStateData

class DirectAddArrowAction (private val arrowDataObject: ArrowDataObject) : IDataAction {

    private var chemArrow: ChemArrow? = null

    override fun execute(data: EditorStateData) {
        val points = mutableMapOf<Int, Vector2d>()

        arrowDataObject.points.forEach { i, vec ->
            points[i] = Vector2d(vec.x, vec.y)
        }

        val a = ChemArrow(points, arrowDataObject.arrowStart, arrowDataObject.arrowEnd)
        data.addArrow(a)
        chemArrow = a
    }

    override fun undo(data: EditorStateData) {
        chemArrow?.let { data.removeArrow(it) }
    }

    override fun redo(data: EditorStateData) {
        chemArrow?.let { data.addArrow(it) }
    }
}