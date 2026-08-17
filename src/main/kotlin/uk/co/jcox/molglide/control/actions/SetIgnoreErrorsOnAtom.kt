package uk.co.jcox.molglide.control.actions

import uk.co.jcox.molglide.control.ChemMolecule
import uk.co.jcox.molglide.control.EditorStateData

class SetIgnoreErrorsOnAtom (
    private val chemAtom: ChemMolecule.ChemAtom,
    private val newValue: Boolean,
) : IDataAction {

    override fun execute(data: EditorStateData) {
        chemAtom.setIgnoreErrors(newValue)
    }

    override fun undo(data: EditorStateData) {
        chemAtom.setIgnoreErrors(!newValue)
    }
}