package uk.co.jcox.molglide.control

import jdk.internal.org.jline.utils.Display
import org.joml.Vector2d
import org.joml.minus
import org.joml.plus
import org.joml.times
import org.openscience.cdk.interfaces.IAtom
import org.openscience.cdk.interfaces.IAtomContainer
import org.openscience.cdk.interfaces.IBond
import uk.co.jcox.molglide.control.ChemMolecule.ChemAtom
import uk.co.jcox.molglide.control.tool.AtomBondTool
import kotlin.math.abs

class UIBuilder (private val data: EditorStateData, private val selectionManager: SelectionManager) {
    private val uiAtoms: MutableList<UIAtom> = mutableListOf()
    private val uiLines: MutableList<UILine> = mutableListOf()

    fun getUIAtoms(): List<UIAtom> = uiAtoms
    fun getUIBonds(): List<UILine> = uiLines

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
                    IBond.Order.SINGLE -> handleSingleStereo(absoluteBond, chemBond)
                    IBond.Order.DOUBLE -> uiLines.addAll(calculatePositionForDoubleBond(absoluteBond, chemBond))
                    IBond.Order.TRIPLE -> uiLines.addAll(calculatePositionForTripleBond(absoluteBond, chemBond))
                    else -> {}
                }
            }
        }
    }


    private fun handleSingleStereo(absoluteBond: UILine, chemBond: ChemMolecule.ChemBond) {
        //If the stereochem is just normal, then just add the absolute bond
        if (chemBond.bond.display == IBond.Display.Solid) {
            uiLines.add(absoluteBond)
            return
        }
        if (chemBond.bond.display == IBond.Display.WedgedHashEnd) {
            addDashedWedgeLines(absoluteBond, chemBond)
            return
        }
    }


    private fun addDashedWedgeLines(absoluteBond: UILine, chemBond: ChemMolecule.ChemBond) {
        val vec = calculateVector(absoluteBond)
        val perp = calculatePerpendicularVector(absoluteBond)
        val currentPos = Vector2d(absoluteBond.startX, absoluteBond.startY)
        val lastPos = Vector2d(absoluteBond.endX, absoluteBond.endY)

        val dist = currentPos.distance(lastPos)
        val toRepeat = (dist / INTER_DASH_DISTANCE).toInt()

        repeat(toRepeat) {
            //As the loop progresses, the size of the dashes increases
            val sideLength = (it / toRepeat.toDouble()) * DASH_DISTANCE

            val newStartX = (currentPos.x + perp.x * sideLength /2)
            val newStartY = (currentPos.y + perp.y * sideLength /2)
            val newEndX = (currentPos.x - perp.x * sideLength /2)
            val newEndY = (currentPos.y - perp.y * sideLength /2)
            uiLines.add(UILine(newStartX, newStartY, newEndX, newEndY))
            currentPos.x += vec.x * INTER_DASH_DISTANCE
            currentPos.y += vec.y * INTER_DASH_DISTANCE
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
    private fun getAbsoluteBond(chemBond: ChemMolecule.ChemBond) : UILine {
        val atomA = ChemAtom(chemBond.bond.getAtom(0), chemBond.molecule)
        val atomB = ChemAtom(chemBond.bond.getAtom(1), chemBond.molecule)
        val aPos = atomA.getPos()
        val bPos = atomB.getPos()
        val aVis = atomA.isVisible()
        val bVis = atomB.isVisible()
        val start = if (bVis) getCappedEnd(aPos, bPos) else bPos
        val end = if (aVis) getCappedEnd(bPos, aPos) else aPos
        val id = chemBond.bond.id
        val uiLine: UILine = UILine(start.x, start.y, end.x, end.y)
        return uiLine
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
     * @param uiLine The basic core bond properties (This should not be added to final UI) since these functions returns
     * a whole list which should be added to the final UI
     *
     * @return The list of new UI bonds which can be added to the final UI
     */
    private fun calculatePositionForTripleBond(uiLine: UILine, chemBond: ChemMolecule.ChemBond): List<UILine> {
        val bondList = mutableListOf<UILine>()
        val perp = calculatePerpendicularVector(uiLine)
        val bondA = applyBondTranslation(uiLine, perp * INTER_BOND_DISTANCE)
        val bondB = applyBondTranslation(uiLine, perp * -INTER_BOND_DISTANCE)

        bondList.add(uiLine)
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
     *  @param uiLine The main bond metrics after clipping where the element label is
     *  @return A list of bonds to add to the UIBonds list
     */
    private fun calculatePositionForDoubleBond(uiLine: UILine, chemBond: ChemMolecule.ChemBond) : List<UILine> {
        val bondList = mutableListOf<UILine>()

        //Create the double bond, and correctly choose the side
        val doubleBond = calculateDoubleBondSide(uiLine, chemBond)
        val doubleUIBond = doubleBond.first
        //Check to see if the double bond should be centred
        val shouldCentre = shouldCentreDoubleBond(chemBond)

        if (shouldCentre) {
            //Have to take all the UiBonds now back by half of the applied vector
            val appliedVector = doubleBond.second
            val newVector = appliedVector * -0.5
            val bondA = applyBondTranslation(uiLine, newVector)
            val bondB = applyBondTranslation(doubleUIBond, newVector)
            bondList.add(bondA)
            bondList.add(bondB)
            return bondList
        }

        //If we don't need to centre, then we need to shorten the double
        //part of the bond instead
        val finalDoubleBond = getBaselineShortening(doubleUIBond, chemBond)

        bondList.add(finalDoubleBond)
        bondList.add(uiLine)

        return bondList
    }


    private fun calculateDoubleBondSide(uiLine: UILine, chemBond: ChemMolecule.ChemBond): Pair<UILine, Vector2d> {
        val perp = calculatePerpendicularVector(uiLine)
        val aVec = perp * INTER_BOND_DISTANCE
        val bVec = perp * -INTER_BOND_DISTANCE
        val testSideA = applyBondTranslation(uiLine, aVec)
        val testSideB = applyBondTranslation(uiLine, bVec)

        //First check if the bond is part of a ring
        if (chemBond.molecule.checkBondInRing(chemBond)) {
            //The bond is now part of the ring
            //Get the fragment it belongs to, and get the centre of that
            val fragment = findFragment(chemBond, chemBond.molecule.getAllFragments()) ?: return Pair(uiLine, Vector2d(0.0, 0.0))
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


    private fun calculateDistance(uiLine: UILine, point: Vector2d): Double {
        val midpoint = Vector2d((uiLine.startX + uiLine.endX) / 2, (uiLine.startY + uiLine.endY) / 2)
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
    private fun getBaselineShortening(doubleUILine: UILine, chemBond: ChemMolecule.ChemBond) : UILine {
        if (chemBond.isTerminal()) {
            return doubleUILine
        }
        val startDouble = Vector2d(doubleUILine.startX, doubleUILine.startY)
        val endDouble = Vector2d(doubleUILine.endX, doubleUILine.endY)
        val newStart = getCappedEnd(startDouble, endDouble, 0.85)
        val newEnd = getCappedEnd(endDouble, startDouble, 0.85)
        return UILine(newStart.x, newStart.y, newEnd.x, newEnd.y)
    }


    /**
     * Given a UIBond this method will calculate the vector perpendicular to the bond line
     * @param uiLine the bond to supply
     * @return the normalised vector
     */
    private fun calculatePerpendicularVector(uiLine: UILine): Vector2d {


        val diff = calculateVector(uiLine)
        val perp = Vector2d(-diff.y, diff.x)
        return perp.normalize()
    }

    private fun calculateVector(uiLine: UILine): Vector2d {
        val start = Vector2d(uiLine.startX, uiLine.startY)
        val end = Vector2d(uiLine.endX, uiLine.endY)

        val diff = end - start
        return diff.normalize()
    }

    private fun applyBondTranslation(uiLine: UILine, vector: Vector2d) : UILine {
        val startX = uiLine.startX + vector.x
        val startY = uiLine.startY + vector.y
        val endX = uiLine.endX + vector.x
        val endY = uiLine.endY + vector.y
        return UILine(startX, startY, endX, endY)
    }

    private fun getCappedEnd(start: Vector2d, end: Vector2d, amount: Double = 0.70): Vector2d {
        val diff = end - start
        val newEnd = start + (diff * amount)
        return newEnd
    }


    private fun clearUI() {
        uiAtoms.clear()
        uiLines.clear()
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
            return UIBondContext(chemBond.bond.order.numeric(), chemBond.midPoint(), chemBond.bond.isAromatic, chemBond.stereo())
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
        private const val INTER_DASH_DISTANCE = AtomBondTool.CONNECTION_DISTANCE / 10.0
        private const val DASH_DISTANCE = AtomBondTool.CONNECTION_DISTANCE / 2.0
    }
}