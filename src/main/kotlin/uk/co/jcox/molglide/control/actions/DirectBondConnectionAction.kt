package uk.co.jcox.molglide.control.actions

import org.apache.jena.sparql.function.library.date
import uk.co.jcox.molglide.control.ChemMolecule
import uk.co.jcox.molglide.control.EditorStateData
import uk.co.jcox.molglide.io.BondDataObject

class DirectBondConnectionAction (
    private val dataBond: BondDataObject,
    private val molecule: ChemMolecule,
    private val atomA: ChemMolecule.ChemAtom,
    private val atomB: ChemMolecule.ChemAtom,
) : IDataAction {

    private lateinit var bond: ChemMolecule.ChemBond

    override fun execute(data: EditorStateData) {
        bond = molecule.formBasicConnection(atomA, atomB)
        bond.setFlip(dataBond.doubleFlip)
        bond.setStereo(dataBond.stereoDisplay)
        molecule.updateBondOrder(bond, dataBond.order)
        bond.bond.setIsAromatic(dataBond.aromatic)
        bond.bond.id = dataBond.editorID
    }

    override fun undo(data: EditorStateData) {
        molecule.removeConnection(bond)
    }

    override fun redo(data: EditorStateData) {
        molecule.directlyAddBond(bond.bond)
    }
}