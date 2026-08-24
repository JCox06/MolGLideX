package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.EditorStateData


/**
 * Import the molecules from one Level's Data to another
 */
class ImportMoleculesAction (
    private val dataToImport: EditorStateData,
) : IDataAction {

    override fun execute(data: EditorStateData) {
        data.addMolecules(dataToImport.getMolecules())
        data.addArrows(dataToImport.getArrows())
    }

    override fun undo(data: EditorStateData) {
        data.removeMolecules(dataToImport.getMolecules())
        data.removeArrows(dataToImport.getArrows())
    }
}