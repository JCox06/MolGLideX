package uk.co.jcox.molglide.control

import uk.co.jcox.molglide.control.actions.IDataAction


class ActionManager (
    private val levelData: EditorStateData
) {

    private val pastActions = ArrayDeque<IDataAction>()
    private val discardedActions = ArrayDeque<IDataAction>()
    var isDirty = true
        private set

    fun executeAction(action: IDataAction) {
        action.execute(levelData)
        pastActions.addLast(action)
        discardedActions.clear()
        isDirty = true
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
            isDirty = true
        }
    }

    fun restoreLastAction() {
        if (discardedActions.isNotEmpty()) {
            val last = discardedActions.removeLast()
            last.redo(levelData)
            pastActions.addLast(last)
            isDirty = true
        }
    }

    fun markNotDirty() {
        isDirty = false
    }
}