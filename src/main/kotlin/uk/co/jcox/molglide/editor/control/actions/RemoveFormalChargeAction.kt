package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.chemengine.FormalChargeWrapper
import uk.co.jcox.molglide.editor.model.EditorStateData

class RemoveFormalChargeAction (
    private val formalCharge: FormalChargeWrapper
) : IDataAction {

    private val original = formalCharge.getCharge()

    override fun execute(data: EditorStateData) {
        data.removeCharge(formalCharge)
        formalCharge.setCharge(0)
    }

    override fun undo(data: EditorStateData) {
        data.addCharge(formalCharge)
        formalCharge.setCharge(original)
    }
}