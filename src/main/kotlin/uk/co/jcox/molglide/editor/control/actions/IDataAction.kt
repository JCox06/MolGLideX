package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.chemengine.MgxAtom

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