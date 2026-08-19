package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.EditorStateData

class BondDeletionAction (val toDelete: ChemMolecule.ChemBond) : IDataAction {

    private val molecule: ChemMolecule = toDelete.molecule


    override fun execute(data: EditorStateData) {
        molecule.removeConnection(toDelete.bond)
    }

    override fun undo(data: EditorStateData) {
        molecule.directlyAddBond(toDelete.bond)
    }
}