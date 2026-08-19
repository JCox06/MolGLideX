package uk.co.jcox.molglide.editor.control.actions

import org.openscience.cdk.interfaces.IBond
import uk.co.jcox.molglide.editor.model.ChemBond
import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.EditorStateData

class UpdateBondOrderAction (private val chemBond: ChemBond, val newOrder: IBond.Order) : IDataAction {

    private val molecule = chemBond.molecule

    private val currentOrder = chemBond.bond.order

    override fun execute(data: EditorStateData) {
        molecule.updateBondOrder(chemBond, newOrder)
    }

    override fun undo(data: EditorStateData) {
        molecule.updateBondOrder(chemBond, currentOrder)
    }
}