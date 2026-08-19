package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.ChemBond
import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.EditorStateData

class UpdateBondAromaticityAction (private val chemBond: ChemBond) : IDataAction {

    override fun execute(data: EditorStateData) {
        chemBond.bond.setIsAromatic(!chemBond.bond.isAromatic)
    }

    override fun undo(data: EditorStateData) {
        execute(data)
    }
}