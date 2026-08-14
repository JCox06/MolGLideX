package uk.co.jcox.molglide.control.actions

import org.checkerframework.checker.units.qual.m
import org.openscience.cdk.interfaces.IBond
import uk.co.jcox.molglide.control.ChemMolecule
import uk.co.jcox.molglide.control.EditorStateData

class UpdateBondOrderAction (private val chemBond: ChemMolecule.ChemBond, val newOrder: IBond.Order) : IDataAction {

    private val molecule = chemBond.molecule

    private val currentOrder = chemBond.bond.order

    override fun execute(data: EditorStateData) {
        molecule.updateBondOrder(chemBond, newOrder)
    }

    override fun undo(data: EditorStateData) {
        molecule.updateBondOrder(chemBond, currentOrder)
    }
}