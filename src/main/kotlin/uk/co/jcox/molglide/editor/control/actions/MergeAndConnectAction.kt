package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.chemengine.MgxAtom
import uk.co.jcox.molglide.editor.model.chemengine.MgxMolecule

class MergeAndConnectAction (private val chemAtomA: MgxAtom, private val chemAtomB: MgxAtom) : IDataAction {


    private val moleculeA = chemAtomA.getMolecule()
    private val moleculeB = chemAtomB.getMolecule()

    private val chemAtomAIndex = moleculeA.indexOf(chemAtomA)
    private val chemAtomBIndex = moleculeB.indexOf(chemAtomB)

    private val newChemAtomAIndex = chemAtomAIndex
    private val newChemAtomBIndex = moleculeA.atoms().size + chemAtomBIndex

    init {
        require(moleculeA != moleculeB) {"Cannot merge two atoms that are already merged/in the same container"}
    }

    private var newMergedMolecule: MgxMolecule? = null


    override fun execute(data: EditorStateData) {
        val newMolecule = moleculeA.copyAndMerge(moleculeB)
        newMergedMolecule = newMolecule
        data.removeMolecule(moleculeA)
        data.removeMolecule(moleculeB)
        data.addMolecule(newMolecule)

        //Form bond between the new CLONED atoms
        newMolecule.addBond(newChemAtomAIndex, newChemAtomBIndex, 1)
    }

    override fun undo(data: EditorStateData) {
        //Remove the CLONED stuff, and add the OLD stuff
        newMergedMolecule?.let { data.removeMolecule(it) }
        data.addMolecule(moleculeA)
        data.addMolecule(moleculeB)
    }

    override fun redo(data: EditorStateData) {
        newMergedMolecule?.let { data.addMolecule(it) }
        data.removeMolecule(moleculeA)
        data.removeMolecule(moleculeB)
    }

}