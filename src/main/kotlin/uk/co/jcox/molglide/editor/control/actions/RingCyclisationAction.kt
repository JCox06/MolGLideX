package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.chemengine.MgxAtom
import uk.co.jcox.molglide.editor.model.chemengine.MgxBond

class RingCyclisationAction (
    private val atomA: MgxAtom,
    private val atomB: MgxAtom,
) : IDataAction{

    private var bond: MgxBond? = null
    private val mgxMolecule = atomA.getMolecule()

    override fun execute(data: EditorStateData) {
        bond = mgxMolecule.addBond(atomA, atomB, 1)
    }

    override fun undo(data: EditorStateData) {
        bond?.let { mgxMolecule.removeBond(it) }
    }

    override fun redo(data: EditorStateData) {
        bond?.let { mgxMolecule.addBond(it) }
    }
}