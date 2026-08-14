package uk.co.jcox.molglide.control.actions

import uk.co.jcox.molglide.control.EditorStateData

class CompoundAction (val actions: List<IDataAction>) : IDataAction {

    override fun execute(data: EditorStateData) {
        actions.forEach { it.execute(data) }
    }

    override fun undo(data: EditorStateData) {
        actions.forEach { it.undo(data) }
    }

    override fun redo(data: EditorStateData) {
        actions.forEach { it.redo(data) }
    }
}