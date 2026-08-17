package uk.co.jcox.molglide.control.actions

import uk.co.jcox.molglide.control.ChemMolecule
import uk.co.jcox.molglide.control.EditorStateData

class BondDeletionAction (val toDelete: ChemMolecule.ChemBond) : IDataAction {

    private val molecule: ChemMolecule = toDelete.molecule


    override fun execute(data: EditorStateData) {
        molecule.removeConnection(toDelete.bond)
    }

    override fun undo(data: EditorStateData) {
        molecule.directlyAddBond(toDelete.bond)
    }
}