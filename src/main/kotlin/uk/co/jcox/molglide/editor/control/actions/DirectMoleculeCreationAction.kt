package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.MolGLideUtils
import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.chemengine.MgxMolecule

class DirectMoleculeCreationAction : IDataAction {

    lateinit var newMolecule: MgxMolecule

    override fun execute(data: EditorStateData) {
        newMolecule = MolGLideUtils.createMolecule()
        data.addMolecule(newMolecule)
    }

    override fun undo(data: EditorStateData) {
        data.removeMolecule(newMolecule)
    }

    override fun redo(data: EditorStateData) {
        data.addMolecule(newMolecule)
    }
}