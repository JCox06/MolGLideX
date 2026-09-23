package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.chemengine.MgxBond

class BondDeletionAction (val toDelete: MgxBond) : IDataAction {

    private val molecule = toDelete.getMolecule()


    override fun execute(data: EditorStateData) {
        molecule.removeBond(toDelete)
    }

    override fun undo(data: EditorStateData) {
        molecule.addBond(toDelete)
    }
}