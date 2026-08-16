package uk.co.jcox.molglide.control.actions

import uk.co.jcox.molglide.control.ChemMolecule
import uk.co.jcox.molglide.control.EditorStateData
import javax.vecmath.Point2d

class MoveAtomAction (
    private val chemAtom: ChemMolecule.ChemAtom,
    private val newPos: Point2d,
    private val oldPos: Point2d,
) : IDataAction {

    override fun execute(data: EditorStateData) {
        chemAtom.atom.point2d = newPos
    }

    override fun undo(data: EditorStateData) {
        chemAtom.atom.point2d = oldPos
    }
}