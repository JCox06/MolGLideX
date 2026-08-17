package uk.co.jcox.molglide.control.actions

import org.checkerframework.checker.units.qual.mol
import uk.co.jcox.molglide.control.ChemMolecule
import uk.co.jcox.molglide.control.EditorStateData


/**
 * Sometimes in the editor, the user can execute actions that leads to a molecule becoming fragmented
 *
 * Fragmented in the sense that the underlying CDK IAtomContainer actually holds to separate molecules
 *
 * This can happen when a user deletes a connecting atom, or connecting bond, that causes the single
 * molecule to become two.
 *
 * This action searches for fragments in the same IAtomContainer, and then partitions the separate fragments into
 * their own new individual IAtomContainers
 *
 * Like any other action, this can be entirely undone
 *
 * This action is commonly called as the last action in a compound action to check for
 * any fragmentation in a molecule
 *
 * */
class PartitionFragmentsAction (private val molecule: ChemMolecule) : IDataAction {

    private var fragments = listOf<ChemMolecule>()

    override fun execute(data: EditorStateData) {
        val isFragmented = molecule.isFragmented()
        if (isFragmented) {
            //First remove the original molecule
            data.removeMolecule(molecule)

            //Then add all the fragments of that molecule
            fragments = molecule.splitIntoFragments()
            data.addMolecules(fragments)
            return
        }
        fragments = emptyList()
    }


    override fun undo(data: EditorStateData) {
        if (fragments.isEmpty()) {
            //Can simply return since the original molecule
            //was not deleted, and there are no fragments to delete
            return
        }
        //Otherwise remove the fragments that were generated and
        //add back the old molecule
        fragments.forEach { data.removeMolecule(it) }
        data.addMolecule(molecule)
    }

    override fun redo(data: EditorStateData) {
        if (fragments.isEmpty()) {
            return
        }

        data.removeMolecule(molecule)
        data.addMolecules(fragments)
    }
}