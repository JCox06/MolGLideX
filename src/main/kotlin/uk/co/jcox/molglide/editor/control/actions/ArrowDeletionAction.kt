package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.ChemArrow
import uk.co.jcox.molglide.editor.model.EditorStateData

class ArrowDeletionAction (
    private val toDelete: ChemArrow
) : IDataAction {

    override fun execute(data: EditorStateData) {
        data.removeArrow(toDelete);
    }

    override fun undo(data: EditorStateData) {
        data.addArrow(toDelete);
    }
}