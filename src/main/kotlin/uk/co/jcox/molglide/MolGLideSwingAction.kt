package uk.co.jcox.molglide

import uk.co.jcox.molglide.editor.model.chemengine.MgxAtom
import uk.co.jcox.molglide.editor.model.chemengine.MgxBond
import javax.swing.AbstractAction

abstract class MolGLideSwingAction(name: String) : AbstractAction(name){

    open fun chemDataChanged(activeSession: EditorSession, currentBond: MgxBond?, currentAtom: MgxAtom?) {

    }
}


abstract class MolGLideSwingBondAction(name: String) : MolGLideSwingAction(name) {

    override fun chemDataChanged(activeSession: EditorSession, currentBond: MgxBond?, currentAtom: MgxAtom?) {
        isEnabled = currentBond != null
    }
}

abstract class MolGLideSwingAtomAction(name: String) : MolGLideSwingAction(name) {

    override fun chemDataChanged(activeSession: EditorSession, currentBond: MgxBond?, currentAtom: MgxAtom?) {
        isEnabled = currentAtom != null
    }
}