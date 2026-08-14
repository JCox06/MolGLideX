package uk.co.jcox.molglide.control.actions

import org.openscience.cdk.interfaces.IBond
import uk.co.jcox.molglide.control.ChemMolecule
import uk.co.jcox.molglide.control.EditorStateData

class AtomDeletionAction (val toDelete: ChemMolecule.ChemAtom) : IDataAction {

    private val chemMolecule = toDelete.molecule

    //Stuff to restore if the action is to be undone
    private var memberBonds = listOf<ChemMolecule.ChemBond>()

    //Fragments created that must be removed if the action is to be undone
    private var fragments = listOf<ChemMolecule>()

    override fun execute(data: EditorStateData) {
        //First delete the atom and connected bonds
        deleteAtomAndBonds()

        //After a deletion, the atomContainer might be fragmented
        //If that is the case, each fragment must belong to its own atom container
        fragments = furtherFragment(data, chemMolecule)
    }

    override fun undo(data: EditorStateData) {
        restoreAtomAndBonds(data)
    }

    private fun deleteAtomAndBonds() {
        //First find a list of bonds that this atom is part of to delete
        memberBonds = chemMolecule.bonds().filter { it.bond.contains(toDelete.atom) }
        memberBonds.forEach {
            chemMolecule.removeConnection(it)
        }
        //Now finally remove the atom
        chemMolecule.removeAtom(toDelete)
    }

    private fun restoreAtomAndBonds(data: EditorStateData) {
        //In Reverse order, if fragments exist, then remove them
        if (fragments.isNotEmpty()) {
            fragments.forEach { data.removeMolecule(it) }

            //Then add back the original singular fragmented atom container
            data.addMolecule(chemMolecule)
        }

        //In Reverse order add the atom, followed by the bonds
        //IMPORTANT: Make sure they are the same original CDK Object!
        chemMolecule.directlyAddAtom(toDelete.atom)
        memberBonds.forEach {
            chemMolecule.directlyAddBond(it.bond)
        }
    }


}