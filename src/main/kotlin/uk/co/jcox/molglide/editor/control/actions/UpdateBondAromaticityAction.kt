package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.EditorStateData

class UpdateBondAromaticityAction (private val chemBond: ChemMolecule.ChemBond) : IDataAction {

    override fun execute(data: EditorStateData) {
        chemBond.bond.setIsAromatic(!chemBond.bond.isAromatic)
    }

    override fun undo(data: EditorStateData) {
        execute(data)
    }
}