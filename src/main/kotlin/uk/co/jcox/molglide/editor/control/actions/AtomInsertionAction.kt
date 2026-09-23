package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.chemengine.MgxAtom
import uk.co.jcox.molglide.editor.model.chemengine.MgxBond

class AtomInsertionAction (
    private val atomInsert: String,
    private val insertTo: MgxAtom,
    private var clickX: Int,
    private var clickY: Int,
) : IDataAction {

    private val chemMolecule = insertTo.getMolecule()

    //Restore Previous state
    private var wasVisible = insertTo.isNotImplicit()

    //Keep track of newly added objects
    var newAtom: MgxAtom? = null
    var newBond: MgxBond? = null


    override fun execute(data: EditorStateData) {
        hideIfCarbon(insertTo)

        val nAtom = chemMolecule.addAtom(atomInsert, clickX.toDouble(), clickY.toDouble())
        val nBond = chemMolecule.addBond(insertTo, nAtom, 1)
        newAtom = nAtom
        newBond = nBond
        hideIfCarbon(nAtom)
    }

    override fun undo(data: EditorStateData) {
        newBond?.let { chemMolecule.removeBond(it) }
        newAtom?.let { chemMolecule.removeAtom(it) }
        insertTo.setNotImplicit(wasVisible)
    }

    override fun redo(data: EditorStateData) {
        newAtom?.let { chemMolecule.addAtom(it) }
        newBond?.let { chemMolecule.addBond(it) }
        hideIfCarbon(insertTo)
    }
}