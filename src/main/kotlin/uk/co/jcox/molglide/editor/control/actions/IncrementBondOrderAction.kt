package uk.co.jcox.molglide.editor.control.actions

import com.github.jsonldjava.shaded.com.google.common.net.InetAddresses.decrement
import com.github.jsonldjava.shaded.com.google.common.net.InetAddresses.increment
import org.checkerframework.checker.units.qual.mol
import org.openscience.cdk.interfaces.IBond
import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.chemengine.MgxBond

class IncrementBondOrderAction (val chemBond: MgxBond) : IDataAction {

    val molecule = chemBond.getMolecule()

    override fun execute(data: EditorStateData) {
        val newOrder = chemBond.getOrder() + 1
        molecule.setBondOrder(chemBond, newOrder)
    }

    override fun undo(data: EditorStateData) {
        val newOrder = chemBond.getOrder() - 1
        molecule.setBondOrder(chemBond, newOrder)
    }

}