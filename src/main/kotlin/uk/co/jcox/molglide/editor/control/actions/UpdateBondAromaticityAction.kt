package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.chemengine.MgxBond

class UpdateBondAromaticityAction (private val chemBond: MgxBond) : IDataAction {

    override fun execute(data: EditorStateData) {
        chemBond.setBondAromaticity(!chemBond.getBondAromaticity())
    }

    override fun undo(data: EditorStateData) {
        execute(data)
    }
}