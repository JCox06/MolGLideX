package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.chemengine.MgxMolecule

class CleanupStructure (private val originalMolecule: MgxMolecule) : IDataAction {


    private val moleculeToClean = originalMolecule
    private var cleanedMolecule: MgxMolecule? = null

    override fun execute(data: EditorStateData) {
        val returnedMolecule = moleculeToClean.clean2DStructure()
        data.removeMolecule(originalMolecule)
        data.addMolecule(returnedMolecule)
        cleanedMolecule = returnedMolecule
    }

    override fun undo(data: EditorStateData) {
        cleanedMolecule?.let { data.removeMolecule(it) }
        data.addMolecule(originalMolecule)
    }

    override fun redo(data: EditorStateData) {
        data.removeMolecule(originalMolecule)
        cleanedMolecule?.let { data.addMolecule(it) }
    }
}