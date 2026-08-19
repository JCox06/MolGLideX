package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.ChemAtom
import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.EditorStateData

class SetIgnoreErrorsOnAtom (
    private val chemAtom: ChemAtom,
    private val newValue: Boolean,
) : IDataAction {

    override fun execute(data: EditorStateData) {
        chemAtom.setIgnoreErrors(newValue)
    }

    override fun undo(data: EditorStateData) {
        chemAtom.setIgnoreErrors(!newValue)
    }
}