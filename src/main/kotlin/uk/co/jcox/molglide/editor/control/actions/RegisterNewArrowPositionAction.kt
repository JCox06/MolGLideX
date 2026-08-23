package uk.co.jcox.molglide.editor.control.actions

import org.joml.Vector2d
import uk.co.jcox.molglide.editor.model.ChemArrow
import uk.co.jcox.molglide.editor.model.EditorStateData

class RegisterNewArrowPositionAction (
    private val chemArrow: ChemArrow,
    private val oldPosition: Vector2d,
    private val positionIndex: Int

) : IDataAction {

    private val newPos = chemArrow.arrowPoints[positionIndex]

    override fun execute(data: EditorStateData) {

    }

    override fun undo(data: EditorStateData) {
        chemArrow.arrowPoints[positionIndex] = oldPosition
    }

    override fun redo(data: EditorStateData) {
        newPos?.let { chemArrow.arrowPoints[positionIndex] = newPos }
    }
}