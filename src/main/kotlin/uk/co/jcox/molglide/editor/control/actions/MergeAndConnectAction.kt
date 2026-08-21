package uk.co.jcox.molglide.editor.control.actions

import org.checkerframework.checker.units.qual.mol
import org.openscience.cdk.smiles.smarts.parser.SMARTSParserConstants.c
import uk.co.jcox.molglide.editor.model.ChemAtom
import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.EditorStateData
import java.util.UUID

class MergeAndConnectAction (private val chemAtomA: ChemAtom, private val chemAtomB: ChemAtom) : IDataAction {


    private val moleculeA = chemAtomA.molecule
    private val moleculeB = chemAtomB.molecule

    private val chemAtomAIndex = chemAtomA.atom.index
    private val chemAtomBIndex = chemAtomB.atom.index

    private val newChemAtomAIndex = chemAtomAIndex
    private val newChemAtomBIndex = moleculeA.atoms().size + chemAtomBIndex

    init {
        if (moleculeA == moleculeB) {
            throw IllegalStateException("Cannot merge two atoms that are already in the same container")
        }
    }

    private var newMergedMolecule: ChemMolecule? = null


    override fun execute(data: EditorStateData) {
        val newMolecule = moleculeA.createNewMergedContainer(moleculeB)
        newMergedMolecule = newMolecule
        data.removeMolecule(moleculeA)
        data.removeMolecule(moleculeB)
        data.addMolecule(newMolecule)

        //Form bond between the new CLONED atoms
        newMolecule.formBasicConnection(newChemAtomAIndex, newChemAtomBIndex)
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