package uk.co.jcox.molglide

import uk.co.jcox.molglide.editor.model.chemengine.MgxAtom
import uk.co.jcox.molglide.editor.model.chemengine.MgxBond

class SwingActionRegistry {

    private val registeredActions: MutableMap<String, MolGLideSwingAction> = mutableMapOf()

    fun registerAction(key: String, action: MolGLideSwingAction) {
        registeredActions[key] = action
    }

    operator fun get (key: String) : MolGLideSwingAction {
        val action = registeredActions[key] ?: throw IllegalStateException("Action requested before was registered: ${key}")
        return action
    }

    fun stateHasChanged(activeSession: EditorSession, currentBond: MgxBond?, currentAtom: MgxAtom?) {
        registeredActions.values.forEach {
            it.chemDataChanged(activeSession, currentBond, currentAtom)
        }
    }
}