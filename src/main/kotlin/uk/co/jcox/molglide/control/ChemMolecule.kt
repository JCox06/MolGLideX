package uk.co.jcox.molglide.control


import org.apache.commons.lang3.DoubleRange
import org.joml.Vector2d
import org.joml.minus
import org.joml.plus
import org.joml.times
import org.openscience.cdk.Atom
import org.openscience.cdk.AtomContainer
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher
import org.openscience.cdk.exception.CDKException
import org.openscience.cdk.interfaces.IAtom
import org.openscience.cdk.interfaces.IAtomContainer
import org.openscience.cdk.interfaces.IBond
import org.openscience.cdk.smiles.smarts.parser.SMARTSParserConstants.a
import org.openscience.cdk.tools.CDKHydrogenAdder
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator
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
            val ui: UIAtom = UIAtom(atom.symbol, atom.point2d.x, atom.point2d.y, calculateTrailGroup(atom), atom.getProperty(TRAILING_POS), atom.getProperty(VISIBLE), atom.id)
            properties.add(ui)
        }
        return properties
    }

    fun getUIBondProperties() : List<UIBond> {
        val properties = mutableListOf<UIBond>()
        container.bonds().forEach { bond ->
            val atomA = ChemAtom(bond.getAtom(0), this)
            val atomB = ChemAtom(bond.getAtom(1), this)

            val aPos = atomA.getPos()
            val bPos = atomB.getPos()
            val aVis = atomA.isVisible()
            val bVis = atomB.isVisible()

            val start = if (bVis) getCappedEnd(aPos, bPos) else bPos
            val end = if (aVis) getCappedEnd(bPos, aPos) else aPos


            val uiBond: UIBond = UIBond(start.x, start.y, end.x, end.y)
            properties.add(uiBond)
        }
        return properties
    }

    fun updateBondOrder(chemBond: ChemBond, newOrder: IBond.Order) {
        chemBond.bond.order = newOrder
        calculateAtomProperties()
    }

    private fun getCappedEnd(start: Vector2d, end: Vector2d): Vector2d {
        val diff = end - start
        val newEnd = start + (diff * 0.70)
        return newEnd
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

    fun findBond(chemAtom1: ChemAtom, chemAtom2: ChemAtom): ChemBond? {
        val ibond = container.getBond(chemAtom1.atom, chemAtom2.atom)
        if (ibond != null) {
            return ChemBond(ibond, this)
        }
        return null
    }

    fun getFormulaString(): String {
        val formula = MolecularFormulaManipulator.getMolecularFormula(container)
        return MolecularFormulaManipulator.getString(formula)
    }

    fun getMolecularWeight(): Double {
        val mass = AtomContainerManipulator.getMass(container)
        return mass
    }

    private fun initDefaultAtomProperties(atom: IAtom) {
        atom.setProperty(VISIBLE, true)
        atom.setProperty(TRAILING_POS, TrailingGroupPosition.RIGHT)
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
        fun setTrailPos(trail: TrailingGroupPosition) {
            atom.setProperty(TRAILING_POS, trail)
        }
        fun getTrailPos() : TrailingGroupPosition {
            return atom.getProperty<TrailingGroupPosition>(TRAILING_POS)
        }
        fun getPos() : org.joml.Vector2d {
            val p2d = atom.point2d
            return Vector2d(p2d.x, p2d.y)
        }

        override fun hashCode(): Int {
            return atom.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            return atom == other
        }
    }

    class ChemBond (
        val bond: IBond,
        val molecule: ChemMolecule,


    ) {
        override fun equals(other: Any?): Boolean {
            return bond == other
        }

        override fun hashCode(): Int {
            return bond.hashCode()
        }
    }


    enum class TrailingGroupPosition (val vec: Vector2d) {
        ABOVE(Vector2d(0.0, 1.0)),
        BELOW(Vector2d(0.0, -1.0)),
        LEFT(Vector2d(-1.0, 0.0)),
        RIGHT(Vector2d(1.0, 0.0)),
    }

    companion object {
        const val VISIBLE = "MOLGLIDE_VISIBLE"
        const val TRAILING_POS = "MOLGLIDE_TRAILING_POS"
    }
}