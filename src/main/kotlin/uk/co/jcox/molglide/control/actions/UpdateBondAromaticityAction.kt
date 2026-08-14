package uk.co.jcox.molglide.control.actions

import uk.co.jcox.molglide.control.ChemMolecule
import uk.co.jcox.molglide.control.EditorStateData

class UpdateBondAromaticityAction (private val chemBond: ChemMolecule.ChemBond) : IDataAction {

    override fun execute(data: EditorStateData) {
        chemBond.bond.setIsAromatic(!chemBond.bond.isAromatic)
    }

    override fun undo(data: EditorStateData) {
        execute(data)
    }
}