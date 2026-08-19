package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.EditorStateData

class ReplaceAtomAction (private val chemAtom: ChemMolecule.ChemAtom, private val toReplace: String
) : IDataAction {

    private val chemMolecule = chemAtom.molecule

    val oldAtom = chemAtom.atom.symbol
    var wasVisible = chemAtom.isVisible()

    override fun execute(data: EditorStateData) {
        chemMolecule.replaceAtom(chemAtom, toReplace)
        hideIfCarbon(chemAtom)
        showIfOther(chemAtom)
    }

    override fun undo(data: EditorStateData) {
        chemMolecule.replaceAtom(chemAtom, oldAtom)
        chemAtom.setVisible(wasVisible)
    }


}