package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.ChemFormalCharge
import uk.co.jcox.molglide.editor.model.EditorStateData

class CreateFormalCharge (
    private val fc: ChemFormalCharge,
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