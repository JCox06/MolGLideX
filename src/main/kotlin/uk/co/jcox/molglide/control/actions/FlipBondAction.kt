package uk.co.jcox.molglide.control.actions

import uk.co.jcox.molglide.control.ChemMolecule
import uk.co.jcox.molglide.control.EditorStateData

class FlipBondAction (
    private val chemBond: ChemMolecule.ChemBond
) : IDataAction {

    override fun execute(data: EditorStateData) {
        chemBond.setFlip(!chemBond.shouldFlip())
    }

    override fun undo(data: EditorStateData) {
        execute(data)
    }
}