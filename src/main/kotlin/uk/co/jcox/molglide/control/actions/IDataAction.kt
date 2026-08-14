package uk.co.jcox.molglide.control.actions

import uk.co.jcox.molglide.control.ChemMolecule
import uk.co.jcox.molglide.control.EditorStateData

interface IDataAction {
    fun execute(data: EditorStateData)
    fun undo(data: EditorStateData)
    fun redo(data: EditorStateData) {
        execute(data)
    }
    fun hideIfCarbon(chemAtom: ChemMolecule.ChemAtom) {
        if (chemAtom.isCarbon()) {
            chemAtom.setVisible(false)
        }
    }
    fun showIfOther(chemAtom: ChemMolecule.ChemAtom) {
        if (!chemAtom.isCarbon()) {
            chemAtom.setVisible(true)
        }
    }

    fun furtherFragment(data: EditorStateData, mol: ChemMolecule): List<ChemMolecule>  {
        val isFragmented = mol.isFragmented()
        if (isFragmented) {
            //First remove the original molecule
            data.removeMolecule(mol)

            //Then add all the fragments of that molecule
            val frags = mol.splitIntoFragments()
            data.addMolecules(frags)
            return frags
        }
        return emptyList()
    }
}