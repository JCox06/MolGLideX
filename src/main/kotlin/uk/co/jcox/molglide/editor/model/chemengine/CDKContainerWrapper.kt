package uk.co.jcox.molglide.editor.model.chemengine

import org.joml.Vector2d
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher
import org.openscience.cdk.exception.CDKException
import org.openscience.cdk.geometry.GeometryUtil
import org.openscience.cdk.graph.ConnectivityChecker
import org.openscience.cdk.interfaces.IAtom
import org.openscience.cdk.interfaces.IAtomContainer
import org.openscience.cdk.ringsearch.RingSearch
import org.openscience.cdk.smiles.SmiFlavor
import org.openscience.cdk.smiles.SmilesGenerator
import org.openscience.cdk.tools.CDKHydrogenAdder
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator
import javax.vecmath.Point2d

class CDKContainerWrapper (
    private val cdkContainer: IAtomContainer,
) : MgxMolecule {

    override fun addAtom(element: String, xPos: Double, yPos: Double): MgxAtom {
        val atom: IAtom = cdkContainer.builder.newAtom()
        atom.symbol = element
        atom.point2d = Point2d(xPos, yPos)
        cdkContainer.addAtom(atom)
        val mgxAtom = CDKAtomWrapper(atom, this)
        initDefaultAtomProperties(mgxAtom)
        calculateChemData()
        return mgxAtom
    }

    override fun addBond(mgxAtomA: MgxAtom, mgxAtomB: MgxAtom, order: Int): MgxBond {
        require(mgxAtomA.getMolecule() == mgxAtomB.getMolecule()) {"Both atoms should have been created from the same molecule"}
        val atomA = downcast(mgxAtomA)
        val atomB = downcast(mgxAtomB)
        val cdkBond = cdkContainer.builder.newBond()
        cdkBond.setAtoms(arrayOf(atomA.getHandle(), atomB.getHandle()))
        cdkContainer.addBond(cdkBond)
        val mgxBond = CDKBondWrapper(cdkBond, this)
        initDefaultBondProperties(mgxBond)
        mgxBond.setStereo(MgxBond.Stereo.NORMAL)
        setBondOrder(mgxBond, order)
        calculateChemData()
        return mgxBond
    }

    override fun addBond(mgxIndexA: Int, mgxIndexB: Int, order: Int): MgxBond {
        val cdkAtomA = cdkContainer.getAtom(mgxIndexA)
        val cdkAtomB = cdkContainer.getAtom(mgxIndexB)
        require(cdkAtomA != null)
        require(cdkAtomB != null)

        val mgxAtomA = CDKAtomWrapper(cdkAtomA, this)
        val mgxAtomB = CDKAtomWrapper(cdkAtomB, this)
        calculateChemData()
        return addBond(mgxAtomA, mgxAtomB, order)
    }

    override fun addAtom(mgxAtom: MgxAtom) {
        require(mgxAtom.getMolecule() == this) {"Cannot re-add an atom that was never part of this molecule"}
        val cdkAtom = downcast(mgxAtom)
        cdkContainer.addAtom(cdkAtom.getHandle())
        calculateChemData()
    }

    override fun addBond(mgxBond: MgxBond) {
        require(mgxBond.getMolecule() == this) {"Cannot re-add a bond that was never part of this molecule"}
        val cdkBond = downcast(mgxBond)
        cdkContainer.addBond(cdkBond.getHandle())
        calculateChemData()
    }

    override fun removeAtom(mgxAtom: MgxAtom) {
        val cdkAtom = downcast(mgxAtom)
        cdkContainer.removeAtom(cdkAtom.getHandle())
        calculateChemData()
    }

    override fun removeBond(mgxBond: MgxBond) {
        val cdkBond = downcast(mgxBond)
        cdkContainer.removeBond(cdkBond.getHandle())
        calculateChemData()
    }

    override fun changeAtomSymbol(mgxAtom: MgxAtom, element: String) {
        val cdkAtom = downcast(mgxAtom)
        cdkAtom.getHandle().symbol = element
        calculateChemData()
    }

    override fun getCanonicalSMILES(): String {
        val SMILESgen = SmilesGenerator(SmiFlavor.Canonical)
        return SMILESgen.create(cdkContainer)
    }

    override fun getInchi(): InchiReturn {
        return InchiReturn("a", InchiStats.ERROR, "NOT IMPLEMENTED YET")
    }

    override fun bonds(): Collection<MgxBond> {
        return cdkContainer.bonds().map { cdkBond -> CDKBondWrapper(cdkBond, this) }
    }

    override fun atoms(): Collection<MgxAtom> {
        return cdkContainer.atoms().map { cdkAtom -> CDKAtomWrapper(cdkAtom, this) }
    }

    private fun initDefaultAtomProperties(mgxAtom: MgxAtom) {
        mgxAtom.setNotImplicit(true)
        mgxAtom.setTrailPos(MgxAtom.TrailingGroupPosition.RIGHT)
        mgxAtom.setIgnoreErrors(false)
        mgxAtom.setTransient(false)
        mgxAtom.setFormalCharge(0)
    }

    private fun initDefaultBondProperties(mgxBond: MgxBond) {
        mgxBond.setFlip(false)
        mgxBond.setTransient(false)
    }

    private fun downcast(mgxAtom: MgxAtom): CDKAtomWrapper {
        require(mgxAtom is CDKAtomWrapper) {"Can't use a non-CDK atom implementation on a CDK container"}
        return mgxAtom
    }

    private fun downcast(mgxBond: MgxBond): CDKBondWrapper {
        require(mgxBond is CDKBondWrapper) {"Can't use a non-CDK bond implementation on a CDK container"}
        return mgxBond
    }

    private fun downcast(mgxMolecule: MgxMolecule): CDKContainerWrapper {
        require(mgxMolecule is CDKContainerWrapper) {"Can't use a non-CDK container implementation on a CDK container"}
        return mgxMolecule
    }

    override fun equals(other: Any?): Boolean {
        return other is CDKContainerWrapper && this.cdkContainer == other.cdkContainer
    }

    override fun hashCode(): Int {
        return cdkContainer.hashCode()
    }

    override fun bondCount(mgxAtom: MgxAtom): Int {
        val cdkAtom = downcast(mgxAtom)
        val bondCount = cdkContainer.getConnectedBondsCount(cdkAtom.getHandle())
        return bondCount
    }

    override fun calculateChemData() {
        try {
            val atomMatcher = CDKAtomTypeMatcher.getInstance(cdkContainer.builder)
            for (atom in cdkContainer.atoms()) {
                with(atom) {
                    atomTypeName = null
                    valency = null
                    hybridization = null
                    formalNeighbourCount = null
                    bondOrderSum = null
                    implicitHydrogenCount = null
                }
                val atomType = atomMatcher.findMatchingAtomType(cdkContainer, atom)
                AtomTypeManipulator.configure(atom, atomType)
            }
            val hAdder = CDKHydrogenAdder.getInstance(cdkContainer.builder)
            hAdder.addImplicitHydrogens(cdkContainer)
        } catch (e: CDKException) {
            println("Error when calculating atom types")
            e.printStackTrace()
        }
    }

    override fun setBondOrder(mgxBond: MgxBond, order: Int) {
        val cdkBond = downcast(mgxBond)
        cdkBond.getHandle().order = CDKBondWrapper.getCDKOrder(order)
        calculateChemData()
    }

    override fun selectables(): Collection<IEditorSelectable> {
        val items = mutableListOf<IEditorSelectable>()
        items.addAll(atoms())
        items.addAll(bonds())
        return items
    }

    override fun clean2DStructure(): MgxMolecule {
        TODO("NOT YET IMPLEMENTED")
    }

    override fun indexOf(mgxAtom: MgxAtom): Int {
        val cdkAtom = downcast(mgxAtom)
        require(cdkContainer.contains(cdkAtom.getHandle()))
        return cdkContainer.indexOf(cdkAtom.getHandle())
    }

    override fun indexOf(mgxBond: MgxBond): Int {
        val cdkBond = downcast(mgxBond)
        require(cdkContainer.contains(cdkBond.getHandle()))
        return cdkContainer.indexOf(cdkBond.getHandle())
    }

    override fun copyAndMerge(mergeWith: MgxMolecule): MgxMolecule {
        require(this != mergeWith) {"Cannot copy and merge into self"}
        val cdkMerger = downcast(mergeWith)

        val cdkNewContainer = this.cdkContainer.clone()
        val cdkNewMerger = cdkMerger.cdkContainer.clone()

        cdkNewContainer.add(cdkNewMerger)
        val newMolecule = CDKContainerWrapper(cdkNewContainer)
        calculateChemData()
        return newMolecule
    }

    override fun isDisconnected(): Boolean {
        return !ConnectivityChecker.isConnected(cdkContainer)
    }

    override fun splitIntoFragments(): Collection<MgxMolecule> {
        var containers = listOf<MgxMolecule>()
        if (!isDisconnected()) {
            return containers
        }
        val cdkFragments = ConnectivityChecker.partitionIntoMolecules(cdkContainer)
        containers = cdkFragments.map { CDKContainerWrapper(it) }
        return containers
    }

    override fun getTemplateBuilder(): MgxTemplateBuilder {
        return CDKTemplaterWrapper(this)
    }

    override fun getSpatialCentre(): Vector2d {
        val centre = GeometryUtil.get2DCenter(cdkContainer)
        require(centre != null)
        return Vector2d(centre.x, centre.y)
    }

    override fun getBond(mgxAtomA: MgxAtom, mgxAtomB: MgxAtom): MgxBond {
        val cdkWrapperA = downcast(mgxAtomA)
        val cdkWrapperB = downcast(mgxAtomB)
        val cdkBond = cdkContainer.getBond(cdkWrapperA.getHandle(), cdkWrapperB.getHandle())
        require(cdkBond != null) {"The required bond did not exsist in this molecule"}
        return CDKBondWrapper(cdkBond, this)
    }

    override fun hasBond(mgxAtomA: MgxAtom, mgxAtomB: MgxAtom): Boolean {
        val cdkWrapperA = downcast(mgxAtomA)
        val cdkWrapperB = downcast(mgxAtomB)
        val cdkBond = cdkContainer.getBond(cdkWrapperA.getHandle(), cdkWrapperB.getHandle()) ?: return false
        return true
    }

    override fun inRing(mgxBond: MgxBond): Boolean {
        val cdkBondWrapper = downcast(mgxBond)
        val ringSearch = RingSearch(cdkContainer)
        return ringSearch.cyclic(cdkBondWrapper.getHandle())
    }


    override fun createRingFragments(): Collection<MgxMolecule> {
        val ringSearch = RingSearch(cdkContainer)
        val fragments = mutableListOf<IAtomContainer>()
        fragments.addAll(ringSearch.fusedRingFragments())
        fragments.addAll(ringSearch.isolatedRingFragments())

        val mgxFragments = fragments.map { CDKContainerWrapper(it) }
        return mgxFragments
    }

    override fun bulkSetTransient(transient: Boolean) {
        atoms().forEach { it.setTransient(transient) }
        bonds().forEach { it.setTransient(transient) }
    }

    override fun getFormulaString(): String {
        val formula = MolecularFormulaManipulator.getMolecularFormula(cdkContainer)
        return MolecularFormulaManipulator.getString(formula)
    }

    override fun getMolecularMass(): Double {
        val mass = AtomContainerManipulator.getMass(cdkContainer)
        return mass
    }

    /**
     * For the internal use only between CDK-like classes
     * @return direct access to the underlying CDK object
     */
    fun getHandle(): IAtomContainer {
        return cdkContainer
    }

    /**
     * For internal use only between CDK-like classes
     * @param atomContainer Raw CDK data to be added to this container
     */
    fun addRawCDKData(atomContainer: IAtomContainer) {
        cdkContainer.add(atomContainer)
        calculateChemData()
    }
}