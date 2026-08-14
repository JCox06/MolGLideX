package uk.co.jcox.molglide.control

import org.joml.Vector2d
import org.joml.minus
import org.joml.plus
import org.joml.times
import org.openscience.cdk.interfaces.IAtom
import org.openscience.cdk.interfaces.IAtomContainer
import org.openscience.cdk.interfaces.IBond
import uk.co.jcox.molglide.control.ChemMolecule.ChemAtom

class UIBuilder (private val data: EditorStateData, private val selectionManager: SelectionManager) {
    private val uiAtoms: MutableList<UIAtom> = mutableListOf()
    private val uiBonds: MutableList<UIBond> = mutableListOf()

    fun getUIAtoms(): List<UIAtom> = uiAtoms
    fun getUIBonds(): List<UIBond> = uiBonds

    fun rebuild() {
        clearUI()
        buildAtomUI()
        buildBondUI()
    }

    private fun buildAtomUI() {
        data.getMolecules().forEach { chemMolecule ->
            chemMolecule.atoms().forEach { chemAtom ->
                val pos = chemAtom.getPos()
                val ui: UIAtom = UIAtom(
                    chemAtom.atom.symbol,
                    pos.x,
                    pos.y,
                    calculateTrailGroup(chemAtom.atom),
                    chemAtom.getTrailPos(),
                    chemAtom.isVisible(),
                    chemAtom.atom.id
                )
                uiAtoms.add(ui)
            }
        }
    }

    private fun calculateTrailGroup(atom: IAtom): String {
        if (atom.implicitHydrogenCount == 1) {
            return "H"
        }
        if (atom.implicitHydrogenCount > 1) {
            return "H${atom.implicitHydrogenCount}"
        }
        return ""
    }

    fun buildBondUI() {
        data.getMolecules().forEach { chemMolecule ->
            chemMolecule.bonds().forEach { chemBond ->

                val absoluteBond = getAbsoluteBond(chemBond)

                when (chemBond.bond.order) {
                    IBond.Order.SINGLE -> uiBonds.add(absoluteBond)
                    IBond.Order.DOUBLE -> uiBonds.addAll(calculatePositionForDoubleBond(absoluteBond, chemBond))
                    IBond.Order.TRIPLE -> uiBonds.addAll(calculatePositionForTripleBond(absoluteBond, chemBond))
                    else -> {}
                }
            }
        }
    }

    /**
     * When calculating bonds, it becomes a bit harder
     * Regardless of the type of bond (double, single, triple, centred double carbonyl), we need
     * to know one core property: Where should the bond start from, and end from.
     *
     * Since all types of bonds do not want to intersect with the text label of the atom
     * If the text label is visible, then we need to cut and reduce the length of the bond by a bit
     *
     * When building the double bonds (and other more complicated bonds), we still need this core information
     *
     * @param chemBond the bond to calculate basic UI properties for
     * @return UI information to show where a bond really starts and really ends
     */
    private fun getAbsoluteBond(chemBond: ChemMolecule.ChemBond) : UIBond {
        val atomA = ChemAtom(chemBond.bond.getAtom(0), chemBond.molecule)
        val atomB = ChemAtom(chemBond.bond.getAtom(1), chemBond.molecule)
        val aPos = atomA.getPos()
        val bPos = atomB.getPos()
        val aVis = atomA.isVisible()
        val bVis = atomB.isVisible()
        val start = if (bVis) getCappedEnd(aPos, bPos) else bPos
        val end = if (aVis) getCappedEnd(bPos, aPos) else aPos
        val id = chemBond.bond.id
        val uiBond: UIBond = UIBond(start.x, start.y, end.x, end.y)
        return uiBond
    }


    /**
     * Calculating the positions for a triple bond is easier than calculating all the edge cases
     * for double bonds.
     *
     * A triple bond is simply the bond metric supplied, plus two additional bonds
     * added either side of each other.
     *
     * No additional information (such as centring) is required!
     *
     * @param uiBond The basic core bond properties (This should not be added to final UI) since these functions returns
     * a whole list which should be added to the final UI
     *
     * @return The list of new UI bonds which can be added to the final UI
     */
    private fun calculatePositionForTripleBond(uiBond: UIBond, chemBond: ChemMolecule.ChemBond): List<UIBond> {
        val bondList = mutableListOf<UIBond>()
        val perp = calculatePerpendicularVector(uiBond)
        val bondA = applyBondTranslation(uiBond, perp * INTER_BOND_DISTANCE)
        val bondB = applyBondTranslation(uiBond, perp * -INTER_BOND_DISTANCE)

        bondList.add(uiBond)
        bondList.add(bondA)
        bondList.add(bondB)
        return bondList
    }


    /**
     * This method calculates the positions of the double bond that is to be created
     * This requires a few considerations such as
     *  -The side of the double bond
     *  -If the double bond should be centred
     *  -If the edges of the double bond should be cut off more than the edges of single bond creating it
     *
     *  @param uiBond The main bond metrics after clipping where the element label is
     *  @return A list of bonds to add to the UIBonds list
     */
    private fun calculatePositionForDoubleBond(uiBond: UIBond, chemBond: ChemMolecule.ChemBond) : List<UIBond> {
        val bondList = mutableListOf<UIBond>()

        //Create the double bond, and correctly choose the side
        val doubleBond = calculateDoubleBondSide(uiBond, chemBond)
        val doubleUIBond = doubleBond.first
        //Check to see if the double bond should be centred
        val shouldCentre = shouldCentreDoubleBond(chemBond)

        if (shouldCentre) {
            //Have to take all the UiBonds now back by half of the applied vector
            val appliedVector = doubleBond.second
            val newVector = appliedVector * -0.5
            val bondA = applyBondTranslation(uiBond, newVector)
            val bondB = applyBondTranslation(doubleUIBond, newVector)
            bondList.add(bondA)
            bondList.add(bondB)
            return bondList
        }

        //If we don't need to centre, then we need to shorten the double
        //part of the bond instead
        val finalDoubleBond = getBaselineShortening(doubleUIBond, chemBond)

        bondList.add(finalDoubleBond)
        bondList.add(uiBond)

        return bondList
    }


    private fun calculateDoubleBondSide(uiBond: UIBond, chemBond: ChemMolecule.ChemBond): Pair<UIBond, Vector2d> {
        val perp = calculatePerpendicularVector(uiBond)
        val aVec = perp * INTER_BOND_DISTANCE
        val bVec = perp * -INTER_BOND_DISTANCE
        val testSideA = applyBondTranslation(uiBond, aVec)
        val testSideB = applyBondTranslation(uiBond, bVec)

        //First check if the bond is part of a ring
        if (chemBond.molecule.checkBondInRing(chemBond)) {
            //The bond is now part of the ring
            //Get the fragment it belongs to, and get the centre of that
            val fragment = findFragment(chemBond, chemBond.molecule.getAllFragments()) ?: return Pair(uiBond, Vector2d(0.0, 0.0))
            val centre = calculateAverageCentre(fragment)

            val distA = calculateDistance(testSideA, centre)
            val distB = calculateDistance(testSideB, centre)

            if (distA > distB) {
                return Pair(testSideB, bVec)
            }
            return Pair(testSideA, aVec)
        }

        //If the double bond is NOT part of a ring, then just pick side A (as default) unless overriden
        return Pair(testSideA, aVec)
    }

    private fun findFragment(chemBond: ChemMolecule.ChemBond, containers: List<IAtomContainer>) : IAtomContainer? {
        return containers.find { it.contains(chemBond.bond) }
    }

    private fun calculateAverageCentre(container: IAtomContainer): Vector2d {
        var atomCount = 0
        var totalX = 0.0
        var totalY = 0.0
        container.atoms().forEach { atom ->
            val point = atom.point2d
            totalX += point.x
            totalY += point.y
            atomCount++
        }
        val avgX = totalX / atomCount
        val avgY = totalY / atomCount
        return Vector2d(avgX, avgY)
    }


    private fun calculateDistance(uiBond: UIBond, point: Vector2d): Double {
        val midpoint = Vector2d((uiBond.startX + uiBond.endX) / 2, (uiBond.startY + uiBond.endY) / 2)
        return midpoint.distance(point)
    }


    /**
     * If a double bond should be centred (as in the case of carbonyls)
     * then we need to check if each bond has this criteria
     *
     * For now, just check to see if one of the atoms is either C (carbonyl) or nitrogen (Immine)
     */
    private fun shouldCentreDoubleBond(chemBond: ChemMolecule.ChemBond) : Boolean {

        if (chemBond.molecule.checkBondInRing(chemBond)) {
            return false
        }
        //Get both atoms
        val heteroatom = chemBond.bond.atoms().find { it.symbol == "O" || it.symbol == "N" }

        return heteroatom != null
    }


    /**
     * By default, all double part of bonds that are NOT terminal, will be shortened by
     * the same amount.
     *
     * This might not be what professional editors use, but it works for now, and also looks nice
     * which is the main thing
     */
    private fun getBaselineShortening(doubleUIBond: UIBond, chemBond: ChemMolecule.ChemBond) : UIBond {
        if (chemBond.isTerminal()) {
            return doubleUIBond
        }
        val startDouble = Vector2d(doubleUIBond.startX, doubleUIBond.startY)
        val endDouble = Vector2d(doubleUIBond.endX, doubleUIBond.endY)
        val newStart = getCappedEnd(startDouble, endDouble, 0.85)
        val newEnd = getCappedEnd(endDouble, startDouble, 0.85)
        return UIBond(newStart.x, newStart.y, newEnd.x, newEnd.y)
    }


    /**
     * Given a UIBond this method will calculate the vector perpendicular to the bond line
     * @param uiBond the bond to supply
     * @return the normalised vector
     */
    private fun calculatePerpendicularVector(uiBond: UIBond): Vector2d {
        val start = Vector2d(uiBond.startX, uiBond.startY)
        val end = Vector2d(uiBond.endX, uiBond.endY)

        val diff = end - start
        val perp = Vector2d(-diff.y, diff.x)
        return perp.normalize()
    }

    private fun applyBondTranslation(uiBond: UIBond, vector: Vector2d) : UIBond {
        val startX = uiBond.startX + vector.x
        val startY = uiBond.startY + vector.y
        val endX = uiBond.endX + vector.x
        val endY = uiBond.endY + vector.y
        return UIBond(startX, startY, endX, endY)
    }

    private fun getCappedEnd(start: Vector2d, end: Vector2d, amount: Double = 0.70): Vector2d {
        val diff = end - start
        val newEnd = start + (diff * amount)
        return newEnd
    }


    private fun clearUI() {
        uiAtoms.clear()
        uiBonds.clear()
    }


    fun getSelectedFormula(): String {
        val s = selectionManager.primarySelection
        if (s is SelectionManager.Type.ActiveAtom) {
            return s.chemAtom.molecule.getFormulaString()
        }
        return ""
    }

    fun isAtomSelected() : Boolean {
        val selection = selectionManager.primarySelection
        if (selection is SelectionManager.Type.ActiveAtom) {
            return true
        }
        return false
    }

    fun getSelectedBond(): UIBondContext? {
        val selection = selectionManager.primarySelection
        if (selection is SelectionManager.Type.ActiveBond) {
            val chemBond = selection.chemBond
            return UIBondContext(chemBond.bond.order.numeric(), chemBond.midPoint(), chemBond.bond.isAromatic)
        }
        return null
    }

    fun getSelectedAtom(): UIAtomContext? {
        val selection = selectionManager.primarySelection
        if (selection is SelectionManager.Type.ActiveAtom) {
            val chemAtom = selection.chemAtom
            return UIAtomContext(chemAtom.isVisible())
        }
        return null
    }

    fun getSelectedWeight(): Double {
        val s = selectionManager.primarySelection
        if (s is SelectionManager.Type.ActiveAtom) {
            return s.chemAtom.molecule.getMolecularWeight()
        }
        return 0.0
    }


    companion object {
        private const val INTER_BOND_DISTANCE = 6.0
    }
}