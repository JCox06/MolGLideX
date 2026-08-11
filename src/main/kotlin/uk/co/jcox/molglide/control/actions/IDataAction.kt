package uk.co.jcox.molglide.control.actions

import uk.co.jcox.molglide.control.EditorStateData

interface IDataAction {
    fun execute(data: EditorStateData)
    fun undo(data: EditorStateData)
    fun redo(data: EditorStateData) {
        execute(data)
    }
}