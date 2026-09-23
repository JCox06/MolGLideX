package uk.co.jcox.molglide.editor.control.actions

import org.joml.Vector2d
import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.chemengine.MgxAtom
import javax.vecmath.Point2d

class MoveAtomAction (
    private val chemAtom: MgxAtom,
    private val newPos: Vector2d,
    private val oldPos: Vector2d,
) : IDataAction {

    override fun execute(data: EditorStateData) {
        chemAtom.setPos(newPos.x, newPos.y)
    }

    override fun undo(data: EditorStateData) {
        chemAtom.setPos(oldPos.x, oldPos.y)
    }
}