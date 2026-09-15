package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.ChemAtom
import uk.co.jcox.molglide.editor.model.EditorStateData

class DecrementFormalChargeAction (private val chemAtom: ChemAtom) : IDataAction {

    override fun execute(data: EditorStateData) {
        chemAtom.setFormalCharge(chemAtom.getFormalCharge() - 1)
    }

    override fun undo(data: EditorStateData) {
        chemAtom.setFormalCharge(chemAtom.getFormalCharge() + 1)

    }

}