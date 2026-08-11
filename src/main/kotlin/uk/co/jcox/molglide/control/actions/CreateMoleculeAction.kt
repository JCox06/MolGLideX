package uk.co.jcox.molglide.control.actions

import uk.co.jcox.molglide.control.ChemMolecule
import uk.co.jcox.molglide.control.EditorStateData

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