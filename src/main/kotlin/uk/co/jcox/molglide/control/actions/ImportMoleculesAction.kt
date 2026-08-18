package uk.co.jcox.molglide.control.actions

import uk.co.jcox.molglide.control.EditorStateData


/**
 * Import the molecules from one Level's Data to another
 */
class ImportMoleculesAction (
    private val dataToImport: EditorStateData,
) : IDataAction {

    override fun execute(data: EditorStateData) {
        data.addMolecules(dataToImport.getMolecules())
    }

    override fun undo(data: EditorStateData) {
        data.removeMolecules(dataToImport.getMolecules())
    }
}