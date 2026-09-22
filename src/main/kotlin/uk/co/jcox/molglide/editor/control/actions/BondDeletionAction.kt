package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.chemengine.ChemBond
import uk.co.jcox.molglide.editor.model.chemengine.ChemMolecule
import uk.co.jcox.molglide.editor.model.EditorStateData

class BondDeletionAction (val toDelete: ChemBond) : IDataAction {

    private val molecule: ChemMolecule = toDelete.molecule


    override fun execute(data: EditorStateData) {
        molecule.removeConnection(toDelete.bond)
    }

    override fun undo(data: EditorStateData) {
        molecule.directlyAddBond(toDelete.bond)
    }
}