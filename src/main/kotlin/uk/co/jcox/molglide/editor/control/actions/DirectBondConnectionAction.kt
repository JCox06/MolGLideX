package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.io.BondDataObject
import uk.co.jcox.molglide.editor.model.chemengine.MgxAtom
import uk.co.jcox.molglide.editor.model.chemengine.MgxBond
import uk.co.jcox.molglide.editor.model.chemengine.MgxMolecule

class DirectBondConnectionAction (
    private val dataBond: BondDataObject,
    private val molecule: MgxMolecule,
    private val atomA: MgxAtom,
    private val atomB: MgxAtom,
) : IDataAction {

    private lateinit var bond: MgxBond

    override fun execute(data: EditorStateData) {
        bond = molecule.addBond(atomA, atomB, dataBond.order)
        bond.setFlip(dataBond.doubleFlip)
        bond.setStereo(dataBond.stereoDisplay)
        bond.setBondAromaticity(dataBond.aromatic)
        bond.setTransient(false)
    }

    override fun undo(data: EditorStateData) {
        molecule.removeBond(bond)
    }

    override fun redo(data: EditorStateData) {
        molecule.addBond(bond)
    }
}