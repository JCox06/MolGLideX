package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.ChemAtom
import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.EditorStateData
import java.util.*

class ApplyAtomOverrideAction (
    private val chemAtom: ChemAtom,
    private val overrideString: String,
    private val overrideAction: (chemMolecule: ChemMolecule, chemAtom: ChemAtom) -> Unit
) : IDataAction {

    private val chemMolecule = chemAtom.molecule
    private val atomIndex = chemMolecule.atoms().indexOf(chemAtom)

    //Since this action changes a lot of data
    //The better way of doing things is to create a copy of this atom first before changing things.

    //On Execute -> Create copy and do action
    //On Undo -> Remove copy, restore original
    //On Redo -> Remove original restore copy
    //=============================================

    //The molecule that is copied and is being worked on
    private var workingMolecule: ChemMolecule? = null
    private var workingChemAtom: ChemAtom? = null

    override fun execute(data: EditorStateData) {
        //Make a note of the index of the chemAtom, and get the copy
        val moleculeCopy = chemMolecule.deepCopy()
        val atomCopy = moleculeCopy.atoms()[atomIndex]

        //Change properties
        atomCopy.setSymbolOverride(overrideString)
        val originalBondSize = moleculeCopy.bonds().size
        val originalAtomSize = moleculeCopy.atoms().size
        overrideAction(moleculeCopy, atomCopy)
        tagNewComponents(moleculeCopy, originalBondSize, originalAtomSize, atomCopy)

        //Apply to data
        data.removeMolecule(chemMolecule)
        data.addMolecule(moleculeCopy)
        workingMolecule = moleculeCopy
        workingChemAtom = atomCopy

        moleculeCopy.calculateAtomProperties()
    }


    override fun undo(data: EditorStateData) {
        //Remove copy, and place back original
        val moleculeCopy = workingMolecule
        if (moleculeCopy != null) {
            data.removeMolecule(moleculeCopy)
            data.addMolecule(chemMolecule)
        }
    }

    override fun redo(data: EditorStateData) {
        //Remove original, add the same copy back
        val moleculeCopy = workingMolecule
        if (moleculeCopy != null) {
            data.removeMolecule(chemMolecule)
            data.addMolecule(moleculeCopy)
        }
    }

    //The main atom (and hidden internal atoms) that are part of an override
    //For instance in Et, the main atom, and the extra CH3 group need to be tagged.
    //This is so if the group happens to be removed, removal is as simple as removing every atom with the same tag
    private fun tagNewComponents(moleculeCopy: ChemMolecule, originalBondSize: Int, originalAtomSize: Int, atomCopy: ChemAtom) {
        val newBondEndIndex = moleculeCopy.bonds().size - 1
        val newAtomEndIndex = moleculeCopy.atoms().size - 1

        val newID = UUID.randomUUID().toString()

        for (i in originalBondSize..newBondEndIndex) {
            val bond = moleculeCopy.bonds()[i]
            bond.setOverrideID(newID)
            bond.setInternalOnly(true)
        }

        for (i in originalAtomSize .. newAtomEndIndex) {
            val atom = moleculeCopy.atoms()[i]
            atom.setOverrideID(newID)
            atom.setInternalOnly(true)
        }

        atomCopy.setOverrideID(newID)
    }
}