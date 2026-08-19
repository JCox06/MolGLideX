package uk.co.jcox.molglide.editor.control

import uk.co.jcox.molglide.editor.control.actions.IDataAction
import uk.co.jcox.molglide.editor.model.EditorStateData


class ActionManager (
    private val levelData: EditorStateData,
    private val dataChanged: () -> Unit = {}
) {

    private val pastActions = ArrayDeque<IDataAction>()
    private val discardedActions = ArrayDeque<IDataAction>()

    fun executeAction(action: IDataAction) {
        action.execute(levelData)
        pastActions.addLast(action)
        discardedActions.clear()
        dataChanged()
    }

    fun canUndo(): Boolean {
        return pastActions.isNotEmpty()
    }

    fun canRedo(): Boolean {
        return discardedActions.isNotEmpty()
    }

    fun undoLastAction() {
        if (pastActions.isNotEmpty()) {
            val last = pastActions.removeLast()
            last.undo(levelData)
            discardedActions.addLast(last)
            dataChanged()
        }
    }

    fun restoreLastAction() {
        if (discardedActions.isNotEmpty()) {
            val last = discardedActions.removeLast()
            last.redo(levelData)
            pastActions.addLast(last)
            dataChanged()
        }
    }
}