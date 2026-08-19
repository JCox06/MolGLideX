package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.EditorStateData

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