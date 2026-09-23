package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.chemengine.MgxAtom
import uk.co.jcox.molglide.editor.model.chemengine.MgxBond

class AtomDeletionAction (val toDelete: MgxAtom) : IDataAction {

    private val chemMolecule = toDelete.getMolecule()

    //Stuff to restore if the action is to be undone
    private var memberBonds = listOf<MgxBond>()


    override fun execute(data: EditorStateData) {
        //First delete the atom and connected bonds
        deleteAtomAndBonds()
    }

    override fun undo(data: EditorStateData) {
        restoreAtomAndBonds(data)
    }


    override fun redo(data: EditorStateData) {
        memberBonds.forEach { chemBond ->
            chemMolecule.removeBond(chemBond)
        }
        chemMolecule.removeAtom(toDelete)
    }

    private fun deleteAtomAndBonds() {
        //First find a list of bonds that this atom is part of to delete
        memberBonds = chemMolecule.bonds().filter { it.contains(toDelete) }
        memberBonds.forEach {
            chemMolecule.removeBond(it)
        }
        //Now finally remove the atom
        chemMolecule.removeAtom(toDelete)
    }

    private fun restoreAtomAndBonds(data: EditorStateData) {

        //In Reverse order add the atom, followed by the bonds
        //IMPORTANT: Make sure they are the same original CDK Object!
        chemMolecule.addAtom(toDelete)
        memberBonds.forEach {
            chemMolecule.addBond(it)
        }
    }


}