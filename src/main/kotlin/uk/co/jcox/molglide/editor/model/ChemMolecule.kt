package uk.co.jcox.molglide.editor.model


import org.apache.jena.sparql.pfunction.library.container
import org.joml.GeometryUtils
import org.joml.Vector2d
import org.openscience.cdk.Atom
import org.openscience.cdk.AtomContainer
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher
import org.openscience.cdk.exception.CDKException
import org.openscience.cdk.geometry.GeometryUtil
import org.openscience.cdk.graph.ConnectivityChecker
import org.openscience.cdk.interfaces.IAtom
import org.openscience.cdk.interfaces.IAtomContainer
import org.openscience.cdk.interfaces.IBond
import org.openscience.cdk.layout.StructureDiagramGenerator
import org.openscience.cdk.ringsearch.RingSearch
import org.openscience.cdk.smiles.SmiFlavor
import org.openscience.cdk.smiles.SmilesGenerator
import org.openscience.cdk.tools.CDKHydrogenAdder
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator
import uk.co.jcox.molglide.editor.control.EditorStateController
import uk.co.jcox.molglide.editor.control.tool.AtomBondTool
import uk.co.jcox.molglide.editor.ui.EditorPanel
import java.util.UUID
import javax.vecmath.Point2d

/**
 * A Handy wrapper for a CDK IAtomContainer
 *
 * Important: All data is actually attached to CDK objects, this just provides helper functions
 *
 *
 * When providing your own IAtomContainer, this class will automatically apply its own properties to the CDK object
 * see ChemMolecule#initDefaultAtomProperties!
 */
class ChemMolecule (
    private val container: IAtomContainer = AtomContainer(),
    initDefaults: Boolean = true
) : MolGLideChemData(container) {

    init {
        container.setProperty(TRANSIENT, false)
        if (initDefaults) {
            container.atoms().forEach { atom ->
                if (atom != null) {
                    initDefaultAtomProperties(atom)
                }
            }
            container.bonds().forEach{bond ->
                if (bond != null) {
                    initDefaultBondProperties(bond)
                }
            }
        }
    }

    fun addAtom(element: String, positionX: Double, positionY: Double) : ChemAtom {
        val atom: IAtom = Atom(element)
        atom.point2d = Point2d(positionX, positionY)
        initDefaultAtomProperties(atom)
        val chemAtom = directlyAddAtom(atom)
        return chemAtom
    }

    fun directlyAddAtom(atom: IAtom): ChemAtom {
        container.addAtom(atom)
        calculateAtomProperties()
        return ChemAtom(atom, this)
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
        cdkBond.display = IBond.Display.Solid
        cdkBond.id = UUID.randomUUID().toString()
        initDefaultBondProperties(cdkBond)
        calculateAtomProperties()
        return ChemBond(cdkBond, this)
    }

    fun formBasicConnection(atomA: Int, atomB: Int): ChemBond {
        val iAtomA = container.getAtom(atomA)
        val iAtomB = container.getAtom(atomB)
        val chemAtomA = ChemAtom(iAtomA, this)
        val chemAtomB = ChemAtom(iAtomB, this)
        return formBasicConnection(chemAtomA, chemAtomB)
    }

    fun removeAtom(atom: ChemAtom) {
        container.removeAtom(atom.atom)
        calculateAtomProperties()
    }

    fun removeConnection(chemBond: ChemBond) {
        removeConnection(chemBond.bond)
    }

    fun removeConnection(bond: IBond) {
        container.removeBond(bond)
        calculateAtomProperties()
    }

    fun directlyAddBond(cdkBond: IBond): ChemBond {
        container.addBond(cdkBond)
        calculateAtomProperties()
        return ChemBond(cdkBond, this)
    }



    fun replaceAtom(chemAtom: ChemAtom, newElement: String) {
        val atom = chemAtom.atom
        atom.symbol = newElement
        calculateAtomProperties()
    }

    fun getCanonicalString(): String {
        val generator = SmilesGenerator(SmiFlavor.Canonical)
        return generator.create(container)
    }

    fun cleanMolecule() : ChemMolecule {
        val middle = GeometryUtil.get2DCenter(this.container)

        val gen = StructureDiagramGenerator()
        gen.molecule = container
        gen.generateCoordinates()
        val newMolecule = gen.molecule

        val targetBondLength = AtomBondTool.CONNECTION_DISTANCE
        val currentBondLength = GeometryUtil.getBondLengthMedian(newMolecule)
        val factor: Double = targetBondLength / currentBondLength
        GeometryUtil.scaleMolecule(newMolecule, factor)

        GeometryUtil.translate2D(newMolecule, middle.x, middle.y)

        return ChemMolecule(newMolecule, false)
    }

    fun updateBondOrder(chemBond: ChemBond, newOrder: Int) {
        val order = when (newOrder) {
            1 -> IBond.Order.SINGLE
            2 -> IBond.Order.DOUBLE
            3 -> IBond.Order.TRIPLE
            else -> IBond.Order.SINGLE
        }
        updateBondOrder(chemBond, order)
    }

    fun updateBondOrder(chemBond: ChemBond, newOrder: IBond.Order) {
        chemBond.bond.order = newOrder
        calculateAtomProperties()
    }

    fun calculateAtomProperties() {
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

    fun checkBondInRing(chemBond: ChemBond) : Boolean{
        val ringSearch: RingSearch = RingSearch(container)
        return ringSearch.cyclic(chemBond.bond)
    }

    fun getAllFragments(): List<IAtomContainer> {
        val ringSearch: RingSearch = RingSearch(container)
        val fragments = mutableListOf<IAtomContainer>()
        fragments.addAll(ringSearch.fusedRingFragments())
        fragments.addAll(ringSearch.isolatedRingFragments())
        return fragments
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

    fun isFragmented(): Boolean {
        return !ConnectivityChecker.isConnected(container)
    }

    fun rotate(x: Double, y: Double, angle: Double) {
        GeometryUtil.rotate(container, Point2d(x, y), angle)
    }

    fun splitIntoFragments() : List<ChemMolecule> {
        if (!isFragmented()) {
            return emptyList()
        }
        val containers = mutableListOf<ChemMolecule>()

        val cdkFragments = ConnectivityChecker.partitionIntoMolecules(container)


        cdkFragments.forEach { fragment ->
            val newMolecule = ChemMolecule(fragment, false)
            containers.add(newMolecule)
        }

        return containers
    }

    private fun initDefaultAtomProperties(atom: IAtom) {
        atom.setProperty(VISIBLE, true)
        atom.setProperty(TRAILING_POS, TrailingGroupPosition.RIGHT)
        atom.setProperty(IGNORE_ERRORS, false)
        atom.setProperty(TRANSIENT, false)
    }

    private fun initDefaultBondProperties(cdkBond: IBond) {
        cdkBond.setProperty(FLIP_BOND, false)
        cdkBond.setProperty(TRANSIENT, false)
    }

    fun deepCopy(): ChemMolecule {
        val deepCopyCDK = container.clone()
        val newChem = ChemMolecule(deepCopyCDK, false)
        return newChem
    }

    override fun equals(other: Any?): Boolean {
        return other is ChemMolecule && this.container == other.container
    }

    override fun hashCode(): Int {
        return container.hashCode()
    }


    fun createNewMergedContainer(merger: ChemMolecule): ChemMolecule {
        val newContainer = this.container.clone()
        val newMerger = merger.container.clone()

        newContainer.add(newMerger)
        val newChemMolecule = ChemMolecule(newContainer, false)
        return newChemMolecule
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
        const val FLIP_BOND = "MOLGLIDE_FLIP_BOND"
        const val IGNORE_ERRORS = "MOLGLIDE_IGNORE_ERRORS"
        const val TRANSIENT = "MOLGLIDE_TRANSIENT"
        const val UNKNOWN = "X"
    }
}