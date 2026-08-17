package uk.co.jcox.molglide.control.actions

import org.checkerframework.checker.units.qual.mol
import org.openscience.cdk.Atom
import org.openscience.cdk.interfaces.IAtom
import uk.co.jcox.molglide.control.ChemMolecule
import uk.co.jcox.molglide.control.EditorStateData
import uk.co.jcox.molglide.io.AtomDataObject
import javax.vecmath.Point2d

class DirectAtomCreationAction (
    private val molecule: ChemMolecule,
    private val dataAtom: AtomDataObject,
) : IDataAction {

    lateinit var newChemAtom: ChemMolecule.ChemAtom

    override fun execute(data: EditorStateData) {
        val atom: IAtom = Atom(dataAtom.symbol)
        atom.point2d = Point2d(dataAtom.worldX, dataAtom.worldY)
        atom.id = dataAtom.editorID

        newChemAtom = ChemMolecule.ChemAtom(atom, molecule)
        newChemAtom.setVisible(dataAtom.isVisible)
        newChemAtom.setTrailPos(dataAtom.hydrogenPos)
        newChemAtom.setIgnoreErrors(dataAtom.ignoreErrors)

        molecule.directlyAddAtom(atom)
    }

    override fun undo(data: EditorStateData) {
        molecule.removeAtom(newChemAtom)
    }

    override fun redo(data: EditorStateData) {
        molecule.directlyAddAtom(newChemAtom.atom)
    }
}