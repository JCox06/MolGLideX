package uk.co.jcox.molglide.editor.control.actions

import org.checkerframework.checker.units.qual.m
import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.EditorStateData

class CleanupStructure (private val chemMolecule: ChemMolecule) : IDataAction {

    private val oldMolecule = chemMolecule
    private var newMolecule: ChemMolecule? = null

    override fun execute(data: EditorStateData) {
        val returned = chemMolecule.cleanMolecule()

        data.removeMolecule(oldMolecule)
        data.addMolecule(returned)
    }

    override fun undo(data: EditorStateData) {
        val m = newMolecule
        if (m != null) {
            data.removeMolecule(m)
            data.addMolecule(oldMolecule)
        }
    }

    override fun redo(data: EditorStateData) {
        val m = newMolecule
        if (m != null) {
            data.removeMolecule(oldMolecule)
            data.addMolecule(m)
        }
    }
}