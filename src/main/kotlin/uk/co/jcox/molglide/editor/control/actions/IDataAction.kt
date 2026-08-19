package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.ChemAtom
import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.EditorStateData

interface IDataAction {
    fun execute(data: EditorStateData)
    fun undo(data: EditorStateData)
    fun redo(data: EditorStateData) {
        execute(data)
    }
    fun hideIfCarbon(chemAtom: ChemAtom) {
        if (chemAtom.isCarbon()) {
            chemAtom.setVisible(false)
        }
    }
    fun showIfOther(chemAtom: ChemAtom) {
        if (!chemAtom.isCarbon()) {
            chemAtom.setVisible(true)
        }
    }
}