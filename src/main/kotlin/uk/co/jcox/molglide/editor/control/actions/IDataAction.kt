package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.chemengine.MgxAtom

/**
 * Represents an action a user can do that can be rolled back, and re-executed
 * This allows undo, and redo functionality.
 *
 * An Action is executed by an ActionManager and modifies the EditorData
 *
 * The ActionManager is usually called from a Tool but this is not always the case
 */
interface IDataAction {
    fun execute(data: EditorStateData)
    fun undo(data: EditorStateData)
    fun redo(data: EditorStateData) {
        execute(data)
    }
    fun hideIfCarbon(chemAtom: MgxAtom) {
        if (chemAtom.isCarbon) {
            chemAtom.setNotImplicit(false)
        }
    }
    fun showIfOther(chemAtom: MgxAtom) {
        if (!chemAtom.isCarbon) {
            chemAtom.setNotImplicit(true)
        }
    }
}