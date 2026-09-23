package uk.co.jcox.molglide.editor.control.actions

import org.openscience.cdk.Atom
import org.openscience.cdk.interfaces.IAtom
import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.io.AtomDataObject
import uk.co.jcox.molglide.editor.model.chemengine.MgxAtom
import uk.co.jcox.molglide.editor.model.chemengine.MgxMolecule
import javax.vecmath.Point2d

class DirectAtomCreationAction (
    private val molecule: MgxMolecule,
    private val dataAtom: AtomDataObject,
) : IDataAction {

    lateinit var newChemAtom: MgxAtom

    override fun execute(data: EditorStateData) {

        newChemAtom = molecule.addAtom(dataAtom.symbol)
        newChemAtom.setNotImplicit(dataAtom.isVisible)
        newChemAtom.setTrailPos(dataAtom.hydrogenPos)
        newChemAtom.setIgnoreErrors(dataAtom.ignoreErrors)
        newChemAtom.setTransient(false)
        newChemAtom.setPos(dataAtom.worldX, dataAtom.worldY)
    }

    override fun undo(data: EditorStateData) {
        molecule.removeAtom(newChemAtom)
    }

    override fun redo(data: EditorStateData) {
        molecule.addAtom(newChemAtom)
    }
}