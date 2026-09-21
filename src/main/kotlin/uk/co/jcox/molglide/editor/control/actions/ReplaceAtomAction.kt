package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.ChemAtom
import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.EditorStateData

class ReplaceAtomAction (chemAtom: ChemAtom, private val toReplace: String
) : IDataAction {

    private val originalMolecule = chemAtom.molecule
    private val atomIndex = originalMolecule.atoms().indexOf(chemAtom)

    private var workingMolecule: ChemMolecule? = null

    override fun execute(data: EditorStateData) {
//        chemAtom.removeSymbolOverride()
//        chemMolecule.replaceAtom(chemAtom, toReplace)
//        hideIfCarbon(chemAtom)
//        showIfOther(chemAtom)

        val moleculeCopy = originalMolecule.deepCopy()
        val atomCopy = moleculeCopy.atoms()[atomIndex]

        atomCopy.removeSymbolOverride()
        moleculeCopy.replaceAtom(atomCopy, toReplace)
        hideIfCarbonAndNotOverride(atomCopy)
        showIfOther(atomCopy)

        data.removeMolecule(originalMolecule)
        data.addMolecule(moleculeCopy)

        workingMolecule = moleculeCopy
    }

    override fun undo(data: EditorStateData) {
        val m = workingMolecule
        if (m != null) {
            data.removeMolecule(m)
            data.addMolecule(originalMolecule)
        }
    }

    override fun redo(data: EditorStateData) {
        val m = workingMolecule
        if (m != null) {
            data.removeMolecule(originalMolecule)
            data.addMolecule(m)
        }
    }
}