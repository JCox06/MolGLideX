package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.ChemAtom
import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.EditorStateData

class ApplyAtomOverrideAction (
    private val chemAtom: ChemAtom,
    private val newSymbol: String,
    private val symbolOverride: String,
    private val chemData: ChemMolecule?,
    private val joinFunc: ((anchor: ChemAtom, atoms: Collection<ChemAtom>) -> Unit)? = null
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

        atomCopy.setSymbolOverride(symbolOverride, chemData)

        if (chemData != null && joinFunc != null) {
            joinFunc(atomCopy, chemData.atoms())
        }
        //Place new chem data

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
}