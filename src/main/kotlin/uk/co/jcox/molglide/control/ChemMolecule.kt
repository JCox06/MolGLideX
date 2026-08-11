package uk.co.jcox.molglide.control

import org.apache.jena.sparql.pfunction.library.container
import org.openscience.cdk.Atom
import org.openscience.cdk.AtomContainer
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher
import org.openscience.cdk.exception.CDKException
import org.openscience.cdk.interfaces.IAtom
import org.openscience.cdk.interfaces.IAtomContainer
import org.openscience.cdk.interfaces.IBond
import org.openscience.cdk.smiles.smarts.parser.SMARTSParserConstants.a
import org.openscience.cdk.tools.CDKHydrogenAdder
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator
import java.util.UUID
import javax.vecmath.Point2d

class ChemMolecule (
    private val container: IAtomContainer = AtomContainer(),
) {

    fun addAtom(element: String, positionX: Double, positionY: Double) : ChemAtom {
        val atom: IAtom = Atom(element)
        atom.id = UUID.randomUUID().toString()
        atom.point2d = Point2d(positionX, positionY)
        initDefaultAtomProperties(atom)
        return directlyAddAtom(atom)
    }

    /**
     * Forms a bond (order of 1) between two atoms from the same atom container
     * @throws UnsupportedOperationException if the atoms belong to different atom containers
     */
    fun formBasicConnection(chemAtom1: ChemAtom, chemAtom2: ChemAtom): ChemBond  {
        if (chemAtom1.molecule != chemAtom2.molecule) {
            throw UnsupportedOperationException("Cannot form a basic connection between atoms of different containers")
        }
        val cdkBond = container.newBond(chemAtom1.atom, chemAtom2.atom, IBond.Order.SINGLE)
        calculateAtomProperties()
        return ChemBond(cdkBond, this)
    }

    fun removeAtom(atom: ChemAtom) {
        container.removeAtom(atom.atom)
        calculateAtomProperties()
    }

    fun removeConnection(chemBond: ChemBond) {
        container.removeBond(chemBond.bond)
        calculateAtomProperties()
    }

    fun directlyAddBond(cdkBond: IBond): ChemBond {
        container.addBond(cdkBond)
        calculateAtomProperties()
        return ChemBond(cdkBond, this)
    }

    fun directlyAddAtom(atom: IAtom): ChemAtom {
        container.addAtom(atom)
        calculateAtomProperties()
        return ChemAtom(atom, this)
    }

    fun replaceAtom(chemAtom: ChemAtom, newElement: String) {
        val atom = chemAtom.atom
        atom.symbol = newElement
        calculateAtomProperties()
    }

    fun getUIProperties() : List<UIAtom> {
        val properties = mutableListOf<UIAtom>()
        container.atoms().forEach { atom ->
            val ui: UIAtom = UIAtom(atom.symbol, atom.point2d.x, atom.point2d.y, calculateTrailGroup(atom), atom.getProperty<Boolean>(VISIBLE), atom.id)
            properties.add(ui)
        }
        return properties
    }

    fun getUIBondProperties() : List<UIBond> {
        val properties = mutableListOf<UIBond>()
        container.bonds().forEach { bond ->
            val atomA = bond.getAtom(0).point2d
            val atomB = bond.getAtom(1).point2d
            val uiBond: UIBond = UIBond(atomA.x, atomA.y, atomB.x, atomB.y)
            properties.add(uiBond)
        }
        return properties
    }

    private fun calculateTrailGroup(atom: IAtom) : String {
        if (atom.implicitHydrogenCount == 1) {
            return "H"
        }
        if (atom.implicitHydrogenCount > 1) {
            return "H${atom.implicitHydrogenCount}"
        }
        return ""
    }

    private fun calculateAtomProperties() {
        try {
            val atomMatcher = CDKAtomTypeMatcher.getInstance(container.builder)
            for (atom in container.atoms()) {
                with(atom) {
                    atomTypeName = null
                    valency = null
                    hybridization = null
                    formalNeighbourCount = null
                    bondOrderSum = null
                    implicitHydrogenCount = null
                }
                val atomType = atomMatcher.findMatchingAtomType(container, atom)
                AtomTypeManipulator.configure(atom, atomType)
            }
            val hAdder = CDKHydrogenAdder.getInstance(container.builder)
            hAdder.addImplicitHydrogens(container)
        } catch (e: CDKException) {

        }

    }


    fun atoms() : List<ChemAtom> {
        val atoms = mutableListOf<ChemAtom>()
        container.atoms().forEach { iAtom ->
            atoms.add(ChemAtom(iAtom, this))
        }
        return atoms
    }

    fun bonds() : List<ChemBond> {
        val bonds = mutableListOf<ChemBond>()
        container.bonds().forEach { iBond ->
            bonds.add(ChemBond(iBond, this))
        }
        return bonds
    }

    private fun initDefaultAtomProperties(atom: IAtom) {
        atom.setProperty(VISIBLE, true)
    }

    class ChemAtom (
        val atom: IAtom,
        val molecule: ChemMolecule,
    ) {
        fun isVisible(): Boolean {
            return atom.getProperty<Boolean>(VISIBLE)
        }
        fun setVisible(visible: Boolean) {
            atom.setProperty(VISIBLE, visible)
        }
        fun isCarbon(): Boolean {
            return atom.symbol == "C"
        }
    }

    class ChemBond (
        val bond: IBond,
        val molecule: ChemMolecule,
    )

    companion object {
        const val VISIBLE = "MOLGLIDE_VISIBLE"
    }
}