package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.ChemAtom
import uk.co.jcox.molglide.editor.model.ChemBond
import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.EditorStateData

class RingCyclisationAction (
    private val atomA: ChemAtom,
    private val atomB: ChemAtom,
) : IDataAction{

    private var bond: ChemBond? = null

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