package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.ChemArrow
import uk.co.jcox.molglide.editor.model.EditorStateData

class RegisterChemArrowAction (
    private val chemArrow: ChemArrow,
) : IDataAction {

    override fun execute(data: EditorStateData) {
        //Register action so assume the arrow has already been added
    }

    override fun undo(data: EditorStateData) {
        data.removeArrow(chemArrow)
    }

    override fun redo(data: EditorStateData) {
        data.addArrow(chemArrow)
    }
}