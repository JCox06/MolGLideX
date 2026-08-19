package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.EditorStateData

class CreateMoleculeAction (
    private val locationX: Int,
    private val locationY: Int,
    private val element: String,

) : IDataAction{

    private var createdMolecule: ChemMolecule? = null

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