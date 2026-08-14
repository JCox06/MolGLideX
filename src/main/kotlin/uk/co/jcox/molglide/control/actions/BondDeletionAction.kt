package uk.co.jcox.molglide.control.actions

import uk.co.jcox.molglide.control.ChemMolecule
import uk.co.jcox.molglide.control.EditorStateData

class BondDeletionAction (val toDelete: ChemMolecule.ChemBond) : IDataAction {

    private val molecule: ChemMolecule = toDelete.molecule

    private var fragments = listOf<ChemMolecule>()


    override fun execute(data: EditorStateData) {
        molecule.removeConnection(toDelete.bond)

        fragments = furtherFragment(data, molecule)
    }

    override fun undo(data: EditorStateData) {
        if (fragments.isNotEmpty()) {
            fragments.forEach { data.removeMolecule(it) }
            //Then add back the original singular fragmented atom container
            data.addMolecule(molecule)
        }
        molecule.directlyAddBond(toDelete.bond)
    }
}