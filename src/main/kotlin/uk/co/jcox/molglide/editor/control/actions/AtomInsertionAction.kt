package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.ChemAtom
import uk.co.jcox.molglide.editor.model.ChemBond
import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.EditorStateData

class AtomInsertionAction (
    private val atomInsert: String,
    private val insertTo: ChemAtom,
    private var clickX: Int,
    private var clickY: Int,
) : IDataAction {

    private val chemMolecule = insertTo.molecule

    //Restore Previous state
    private var wasVisible = insertTo.isVisible()

    //Keep track of newly added objects
    var newAtom: ChemAtom? = null
    var newBond: ChemBond? = null


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


}