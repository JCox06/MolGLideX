package uk.co.jcox.molglide.control.actions

import uk.co.jcox.molglide.control.ChemMolecule
import uk.co.jcox.molglide.control.EditorStateData
import javax.vecmath.Point2d

class TranslateAtomAction (
    private val chemAtom: ChemMolecule.ChemAtom,
    private val dx: Double,
    private val dy: Double,
) : IDataAction {

    override fun execute(data: EditorStateData) {
        chemAtom.atom.point2d.x += dx
        chemAtom.atom.point2d.y += dy
    }

    override fun undo(data: EditorStateData) {
        chemAtom.atom.point2d.x -= dx
        chemAtom.atom.point2d.y -= dy
    }
}