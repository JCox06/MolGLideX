package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.chemengine.MgxAtom

class TranslateAtomAction (
    private val chemAtom: MgxAtom,
    private val dx: Double,
    private val dy: Double,
) : IDataAction {

    override fun execute(data: EditorStateData) {
        val original = chemAtom.getPos()
        val newX = original.x + dx
        val newY = original.y + dy
        chemAtom.setPos(newX, newY)
    }

    override fun undo(data: EditorStateData) {
        val new = chemAtom.getPos()
        val oldX = new.x - dx
        val oldY = new.y - dy
        chemAtom.setPos(oldX, oldY)
    }
}