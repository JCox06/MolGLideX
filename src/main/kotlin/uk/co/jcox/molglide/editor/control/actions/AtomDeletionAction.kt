package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.ChemAtom
import uk.co.jcox.molglide.editor.model.ChemBond
import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.EditorStateData

class AtomDeletionAction (val toDelete: ChemAtom) : IDataAction {

    private val chemMolecule = toDelete.molecule

    //Stuff to restore if the action is to be undone
    private var memberBonds = listOf<ChemBond>()


    override fun execute(data: EditorStateData) {
        //First delete the atom and connected bonds
        deleteAtomAndBonds()
    }

    override fun undo(data: EditorStateData) {
        restoreAtomAndBonds(data)
    }


    override fun redo(data: EditorStateData) {
        memberBonds.forEach { chemBond ->
            chemMolecule.removeConnection(chemBond)
        }
        chemMolecule.removeAtom(toDelete)
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

        //In Reverse order add the atom, followed by the bonds
        //IMPORTANT: Make sure they are the same original CDK Object!
        chemMolecule.directlyAddAtom(toDelete.atom)
        memberBonds.forEach {
            chemMolecule.directlyAddBond(it.bond)
        }
    }


}