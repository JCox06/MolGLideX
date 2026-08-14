package uk.co.jcox.molglide.control.actions

import uk.co.jcox.molglide.control.ChemMolecule
import uk.co.jcox.molglide.control.EditorStateData

class ToggleAtomVisibilityAction (private val chemAtom: ChemMolecule.ChemAtom) : IDataAction {

    override fun execute(data: EditorStateData) {
        chemAtom.setVisible(!chemAtom.isVisible())
    }

    override fun undo(data: EditorStateData) {
        execute(data)
    }
}