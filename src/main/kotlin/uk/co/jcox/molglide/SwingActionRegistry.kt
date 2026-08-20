package uk.co.jcox.molglide

import uk.co.jcox.molglide.editor.model.ChemAtom
import uk.co.jcox.molglide.editor.model.ChemBond
import javax.swing.AbstractAction

class SwingActionRegistry {

    private val registeredActions: MutableMap<String, MolGLideSwingAction> = mutableMapOf()

    fun registerAction(key: String, action: MolGLideSwingAction) {
        registeredActions[key] = action
    }

    operator fun get (key: String) : MolGLideSwingAction {
        val action = registeredActions[key] ?: throw IllegalStateException("Action requested before was registered: ${key}")
        return action
    }

    fun stateHasChanged(activeSession: EditorSession, currentBond: ChemBond?, currentAtom: ChemAtom?) {
        registeredActions.values.forEach {
            it.chemDataChanged(activeSession, currentBond, currentAtom)
        }
    }
}