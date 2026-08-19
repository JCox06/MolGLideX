package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.ChemAtom
import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.EditorStateData

class TranslateAtomAction (
    private val chemAtom: ChemAtom,
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