package uk.co.jcox.molglide.control.actions

import uk.co.jcox.molglide.control.ChemMolecule
import uk.co.jcox.molglide.control.EditorStateData

class AtomInsertionAction (
    private val atomInsert: String,
    private val insertTo: ChemMolecule.ChemAtom,
    private var clickX: Int,
    private var clickY: Int,
) : IDataAction {

    private val chemMolecule = insertTo.molecule

    //Restore Previous state
    private var wasVisible = insertTo.isVisible()

    //Keep track of newly added objects
    var newAtom: ChemMolecule.ChemAtom? = null
    var newBond: ChemMolecule.ChemBond? = null


    override fun execute(data: EditorStateData) {
        hideIfCarbon(insertTo)

        val nAtom = chemMolecule.addAtom(atomInsert, clickX.toDouble(), clickY.toDouble())
        val nBond = chemMolecule.formBasicConnection(insertTo, nAtom)
        newAtom = nAtom
        newBond = nBond
        hideIfCarbon(nAtom)
    }

    override fun undo(data: EditorStateData) {
        newBond?.let { chemMolecule.removeConnection(it) }
        newAtom?.let { chemMolecule.removeAtom(it) }
        insertTo.setVisible(wasVisible)
    }

    override fun redo(data: EditorStateData) {
        newAtom?.let { chemMolecule.directlyAddAtom(it.atom) }
        newBond?.let { chemMolecule.directlyAddBond(it.bond) }
        hideIfCarbon(insertTo)
    }

    private fun hideIfCarbon(chemAtom: ChemMolecule.ChemAtom) {
        if (chemAtom.isCarbon()) {
            chemAtom.setVisible(false)
        }
    }
}