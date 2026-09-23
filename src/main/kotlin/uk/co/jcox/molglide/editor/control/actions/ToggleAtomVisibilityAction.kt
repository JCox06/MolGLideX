package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.chemengine.MgxAtom

class ToggleAtomVisibilityAction (private val chemAtom: MgxAtom) : IDataAction {

    override fun execute(data: EditorStateData) {
        chemAtom.setNotImplicit(!chemAtom.isNotImplicit())
    }

    override fun undo(data: EditorStateData) {
        execute(data)
    }
}