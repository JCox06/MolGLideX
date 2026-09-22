package uk.co.jcox.molglide

import uk.co.jcox.molglide.editor.model.chemengine.ChemAtom
import uk.co.jcox.molglide.editor.model.chemengine.ChemBond
import javax.swing.AbstractAction

abstract class MolGLideSwingAction(name: String) : AbstractAction(name){

    open fun chemDataChanged(activeSession: EditorSession, currentBond: ChemBond?, currentAtom: ChemAtom?) {

    }
}


abstract class MolGLideSwingBondAction(name: String) : MolGLideSwingAction(name) {

    override fun chemDataChanged(activeSession: EditorSession, currentBond: ChemBond?, currentAtom: ChemAtom?) {
        isEnabled = currentBond != null
    }
}

abstract class MolGLideSwingAtomAction(name: String) : MolGLideSwingAction(name) {

    override fun chemDataChanged(activeSession: EditorSession, currentBond: ChemBond?, currentAtom: ChemAtom?) {
        isEnabled = currentAtom != null
    }
}