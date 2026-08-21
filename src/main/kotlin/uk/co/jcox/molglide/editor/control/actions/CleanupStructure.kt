package uk.co.jcox.molglide.editor.control.actions

import org.apache.jena.sparql.function.library.date
import org.checkerframework.checker.units.qual.mol
import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.EditorStateData

class CleanupStructure (private val originalMolecule: ChemMolecule) : IDataAction {


    private val moleculeToClean = originalMolecule.deepCopy()
    private var cleanedMolecule: ChemMolecule? = null

    override fun execute(data: EditorStateData) {
        val returnedMolecule = moleculeToClean.cleanMolecule()
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