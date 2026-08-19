package uk.co.jcox.molglide.editor.control.actions

import org.openscience.cdk.interfaces.IBond
import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.EditorStateData

class IncrementBondOrderAction (val chemBond: ChemMolecule.ChemBond) : IDataAction {

    val molecule = chemBond.molecule

    override fun execute(data: EditorStateData) {
        val original = chemBond.bond.order
        val new = increment(original)

        molecule.updateBondOrder(chemBond, new)
    }

    override fun undo(data: EditorStateData) {
        val currentOrder = chemBond.bond.order
        val original = decrement(currentOrder)

        molecule.updateBondOrder(chemBond, original)
    }


    private fun increment(order: IBond.Order) : IBond.Order {
        return when (order) {
            IBond.Order.SINGLE -> IBond.Order.DOUBLE
            IBond.Order.DOUBLE -> IBond.Order.TRIPLE
            IBond.Order.TRIPLE -> IBond.Order.SINGLE
            else -> IBond.Order.SINGLE
        }
    }

    private fun decrement(order: IBond.Order) : IBond.Order {
        return when (order) {
            IBond.Order.SINGLE -> IBond.Order.TRIPLE
            IBond.Order.DOUBLE -> IBond.Order.SINGLE
            IBond.Order.TRIPLE -> IBond.Order.DOUBLE
            else -> IBond.Order.SINGLE
        }
    }
}