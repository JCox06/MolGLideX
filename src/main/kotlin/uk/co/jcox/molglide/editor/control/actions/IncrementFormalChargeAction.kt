package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.chemengine.MgxAtom

class IncrementFormalChargeAction (private val chemAtom: MgxAtom) : IDataAction {

    override fun execute(data: EditorStateData) {
        chemAtom.setFormalCharge(chemAtom.getFormalCharge() + 1)
    }

    override fun undo(data: EditorStateData) {
        chemAtom.setFormalCharge(chemAtom.getFormalCharge() - 1)

    }
}