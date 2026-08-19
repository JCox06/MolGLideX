package uk.co.jcox.molglide.editor.control.actions

import org.openscience.cdk.Atom
import org.openscience.cdk.interfaces.IAtom
import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.io.AtomDataObject
import uk.co.jcox.molglide.editor.model.ChemAtom
import javax.vecmath.Point2d

class DirectAtomCreationAction (
    private val molecule: ChemMolecule,
    private val dataAtom: AtomDataObject,
) : IDataAction {

    lateinit var newChemAtom: ChemAtom

    override fun execute(data: EditorStateData) {
        val atom: IAtom = Atom(dataAtom.symbol)
        atom.point2d = Point2d(dataAtom.worldX, dataAtom.worldY)


        newChemAtom = ChemAtom(atom, molecule)
        newChemAtom.setVisible(dataAtom.isVisible)
        newChemAtom.setTrailPos(dataAtom.hydrogenPos)
        newChemAtom.setIgnoreErrors(dataAtom.ignoreErrors)
        newChemAtom.setTransient(false)

        molecule.directlyAddAtom(atom)
    }

    override fun undo(data: EditorStateData) {
        molecule.removeAtom(newChemAtom)
    }

    override fun redo(data: EditorStateData) {
        molecule.directlyAddAtom(newChemAtom.atom)
    }
}