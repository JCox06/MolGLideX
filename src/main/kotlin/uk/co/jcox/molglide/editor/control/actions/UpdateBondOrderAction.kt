package uk.co.jcox.molglide.editor.control.actions

import org.openscience.cdk.interfaces.IBond
import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.chemengine.MgxBond

class UpdateBondOrderAction (private val chemBond: MgxBond, val newOrder: Int) : IDataAction {

    private val molecule = chemBond.getMolecule()

    private val currentOrder = chemBond.getOrder()

    override fun execute(data: EditorStateData) {
        molecule.setBondOrder(chemBond, newOrder)
    }

    override fun undo(data: EditorStateData) {
        molecule.setBondOrder(chemBond, currentOrder)
    }
}