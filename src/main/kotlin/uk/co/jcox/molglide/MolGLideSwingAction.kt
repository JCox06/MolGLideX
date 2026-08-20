package uk.co.jcox.molglide

import uk.co.jcox.molglide.editor.model.ChemAtom
import uk.co.jcox.molglide.editor.model.ChemBond
import java.awt.event.ActionEvent
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