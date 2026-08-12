package uk.co.jcox.molglide.control.actions

import uk.co.jcox.molglide.control.ChemMolecule
import uk.co.jcox.molglide.control.EditorStateData

class RingCyclisationAction (
    private val atomA: ChemMolecule.ChemAtom,
    private val atomB: ChemMolecule.ChemAtom,
) : IDataAction{

    private var bond: ChemMolecule.ChemBond? = null

    override fun execute(data: EditorStateData) {
        bond = atomA.molecule.formBasicConnection(atomA, atomB)
    }

    override fun undo(data: EditorStateData) {
        bond?.let { atomA.molecule.removeConnection(it) }
    }

    override fun redo(data: EditorStateData) {
        bond?.let { atomB.molecule.directlyAddBond(it.bond) }
    }
}