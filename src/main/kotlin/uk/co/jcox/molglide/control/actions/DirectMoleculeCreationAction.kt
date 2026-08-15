package uk.co.jcox.molglide.control.actions

import uk.co.jcox.molglide.control.ChemMolecule
import uk.co.jcox.molglide.control.EditorStateData

class DirectMoleculeCreationAction : IDataAction {

    lateinit var newMolecule: ChemMolecule

    override fun execute(data: EditorStateData) {
        newMolecule = ChemMolecule()

        data.addMolecule(newMolecule)
    }

    override fun undo(data: EditorStateData) {
        data.removeMolecule(newMolecule)
    }

    override fun redo(data: EditorStateData) {
        data.addMolecule(newMolecule)
    }
}