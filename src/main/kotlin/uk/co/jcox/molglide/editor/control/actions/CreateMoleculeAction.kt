package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.chemengine.MgxMolecule

class CreateMoleculeAction (
    private val locationX: Int,
    private val locationY: Int,
    private val element: String,

) : IDataAction{

    private var createdMolecule: MgxMolecule? = null

    override fun execute(data: EditorStateData) {
        createdMolecule = data.createMolecule(element, locationX, locationY)
    }

    override fun undo(data: EditorStateData) {
        createdMolecule?.let { data.removeMolecule(it) }
    }

    override fun redo(data: EditorStateData) {
        createdMolecule?.let { data.addMolecule(it) }
    }
}