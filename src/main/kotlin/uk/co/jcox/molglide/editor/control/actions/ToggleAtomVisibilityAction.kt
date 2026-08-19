package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.EditorStateData

class ToggleAtomVisibilityAction (private val chemAtom: ChemMolecule.ChemAtom) : IDataAction {

    override fun execute(data: EditorStateData) {
        chemAtom.setVisible(!chemAtom.isVisible())
    }

    override fun undo(data: EditorStateData) {
        execute(data)
    }
}