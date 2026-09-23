package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.chemengine.MgxAtom

class ReplaceAtomAction (private val chemAtom: MgxAtom, private val toReplace: String
) : IDataAction {

    private val chemMolecule = chemAtom.getMolecule()

    val oldAtom = chemAtom.getSymbol()
    var wasVisible = chemAtom.isNotImplicit()

    override fun execute(data: EditorStateData) {
        chemMolecule.changeAtomSymbol(chemAtom, toReplace)
        hideIfCarbon(chemAtom)
        showIfOther(chemAtom)
    }

    override fun undo(data: EditorStateData) {
        chemMolecule.changeAtomSymbol(chemAtom, oldAtom)
        chemAtom.setNotImplicit(wasVisible)
    }
}