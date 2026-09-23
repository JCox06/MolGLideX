package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.chemengine.FormalChargeWrapper
import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.chemengine.MgxFormalCharge

class CreateFormalCharge (
    private val fc: MgxFormalCharge,
) : IDataAction {

    private val original = fc.getCharge()

    override fun execute(data: EditorStateData) {
        data.addCharge(fc)
        fc.setCharge(original)
    }

    override fun undo(data: EditorStateData) {
        fc.setCharge(0)
        data.removeCharge(fc)
    }
}