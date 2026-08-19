package uk.co.jcox.molglide.control

import org.joml.Vector2d
import org.joml.minus
import org.joml.plus
import org.joml.times
import org.openscience.cdk.interfaces.IAtom
import org.openscience.cdk.interfaces.IAtomContainer
import org.openscience.cdk.interfaces.IBond
import org.openscience.cdk.smiles.smarts.parser.SMARTSParserConstants.r
import uk.co.jcox.molglide.StereoChem
import uk.co.jcox.molglide.control.ChemMolecule.ChemAtom
import uk.co.jcox.molglide.control.tool.AtomBondTool
import kotlin.math.roundToInt

class UIDataBuilder (private val data: EditorStateData, private val selectionManager: SelectionManager) {


    private val uiComponents: MutableMap<ChemMolecule.MolGLideChemData, AbstractUIComponent> = mutableMapOf()

    fun getUIData(): Collection<AbstractUIComponent> {
        return uiComponents.values
    }


    /**
     * Takes all the complicated CDK chemistry data, and transforms it into simple primitives
     * such as lines, bits of text, polygons (triangles).
     *
     * The specific rebuild operation can be customised to meet the specific criteria
     *
     * @param fullBuild Refreshes all the UI (including transient components)
     *
     * Transient components are components that have the potential to update every frame. Since the majority of the UI is
     * static between frames, and only changes on actions, the main UI is therefore only built when the ActionManager recieves a new action.
     *
     * Components that are marked as transient are added to separate special lists which are chosen to update every frame
     */
    fun rebuild(fullBuild: Boolean = false) {
        if (fullBuild) uiComponents.clear()

        buildAtomUI(fullBuild)
        buildBondUI(fullBuild)
    }


    private fun buildAtomUI(fullBuild: Boolean) {
        data.getMolecules().forEach { chemMolecule ->
            chemMolecule.atoms().forEach { chemAtom ->
                uiComponents[chemAtom]?.selected = selectionManager.isSelected(chemAtom)
                if (!fullBuild && !chemAtom.isTransient()) {
                    return@forEach
                }
                val ui = buildUIAtom(chemAtom)
                uiComponents[chemAtom] = ui
            }
        }
    }


    private fun buildUIAtom(chemAtom: ChemAtom) : UIAtom {
        val pos = chemAtom.getPos()


        val ui: UIAtom = UIAtom(
            chemAtom.atom.symbol,
            pos.x,
            pos.y,
            calculateTrailGroup(chemAtom.atom),
            chemAtom.getTrailPos(),
            chemAtom.isVisible(),
            selectionManager.isSelected(chemAtom),
            checkForAtomErrors(chemAtom),
            chemAtom.shouldIgnoreErrors(),
        )
        return ui
    }

    private fun checkForAtomErrors(chemAtom: ChemAtom): Boolean {
        if (chemAtom.atom.atomTypeName == ChemMolecule.UNKNOWN) {
            return true
        }
        return false
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

    fun buildBondUI(fullBuild: Boolean) {
        data.getMolecules().forEach { chemMolecule ->
            chemMolecule.bonds().forEach { chemBond ->

                uiComponents[chemBond]?.selected = selectionManager.isSelected(chemBond)

                if (!chemBond.isTransient() && !fullBuild) {
                    return@forEach
                }

                val absoluteBond = getAbsoluteBond(chemBond)
                val bondComponents = mutableListOf<AbstractUIComponent>()

                when (chemBond.bond.order) {
                    IBond.Order.SINGLE -> handleSingleStereo(absoluteBond, chemBond, bondComponents)
                    IBond.Order.DOUBLE -> bondComponents.addAll(calculatePositionForDoubleBond(absoluteBond, chemBond))
                    IBond.Order.TRIPLE -> bondComponents.addAll(calculatePositionForTripleBond(absoluteBond, chemBond))
                    else -> {}
                }
                val uiInfo = buildUIBond(chemBond, bondComponents)
                uiComponents[chemBond] = uiInfo }
        }
    }


    private fun handleSingleStereo(absoluteBond: UILine, chemBond: ChemMolecule.ChemBond, bondComponents: MutableList<AbstractUIComponent>) {
        //If the stereochem is just normal, then just add the absolute bond
        val s = chemBond.stereo()
        if (s == StereoChem.NORMAL) {
            bondComponents.add(absoluteBond)
            return
        }
        if (s == StereoChem.DASHED) {
            addDashedWedgeLines(absoluteBond, chemBond, bondComponents)
            return
        }

        if (s == StereoChem.WEDGED) {
            addWedgedBonds(absoluteBond, chemBond, bondComponents)
            return
        }
    }

    private fun addWedgedBonds(absoluteBond: UILine, chemBond: ChemMolecule.ChemBond, bondComponents: MutableList<AbstractUIComponent>) {
        val perp = calculatePerpendicularVector(absoluteBond)

        val startX = absoluteBond.startX
        val startY = absoluteBond.startY
        val v1 = Vector2d(startX, startY)

        val midPointEndX = absoluteBond.endX
        val midPointEndY = absoluteBond.endY

        val v2x = (midPointEndX + perp.x * DASH_DISTANCE / 2)
        val v2y = (midPointEndY + perp.y * DASH_DISTANCE / 2)
        val v2 = Vector2d(v2x, v2y)

        val v3x = (midPointEndX - perp.x * DASH_DISTANCE / 2)
        val v3y = (midPointEndY - perp.y * DASH_DISTANCE / 2)
        val v3 = Vector2d(v3x, v3y)

        bondComponents.add(UITriangle(v3, v2, v1))
    }

    private fun addDashedWedgeLines(absoluteBond: UILine, chemBond: ChemMolecule.ChemBond, bondComponents: MutableList<AbstractUIComponent>) {
        val vec = calculateVector(absoluteBond)
        val perp = calculatePerpendicularVector(absoluteBond)
        val currentPos = Vector2d(absoluteBond.startX, absoluteBond.startY)
        val lastPos = Vector2d(absoluteBond.endX, absoluteBond.endY)


        val dist = currentPos.distance(lastPos)
        val linesToDraw: Double = (dist / INTER_DASH_DISTANCE) //Problem: Need this to be integer
        val roundedLinesToDraw = linesToDraw.roundToInt()
        val relaxedInterDashDistance = (dist / linesToDraw).toInt()

        //The total number of lines to draw, needs to be a perfect integer
        //This implementation produces an okay-ish result. I'm happy with it for now
        //but todo need to rethink how dashed lines should be drawn

        repeat(roundedLinesToDraw + 1) {
            //As the loop progresses, the size of the dashes increases
            val sideLength = (it / roundedLinesToDraw.toDouble()) * DASH_DISTANCE

            val newStartX = (currentPos.x + perp.x * sideLength /2)
            val newStartY = (currentPos.y + perp.y * sideLength /2)
            val newEndX = (currentPos.x - perp.x * sideLength /2)
            val newEndY = (currentPos.y - perp.y * sideLength /2)
            bondComponents.add(UILine(newStartX, newStartY, newEndX, newEndY))
            currentPos.x += vec.x * relaxedInterDashDistance
            currentPos.y += vec.y * relaxedInterDashDistance
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
        val atomA = ChemAtom(chemBond.bond.begin, chemBond.molecule)
        val atomB = ChemAtom(chemBond.bond.end, chemBond.molecule)
        val aPos = atomA.getPos()
        val bPos = atomB.getPos()
        val aVis = atomA.isVisible()
        val bVis = atomB.isVisible()
        val start = if (aVis) getCappedEnd(bPos, aPos) else aPos
        val end = if (bVis) getCappedEnd(aPos, bPos) else bPos
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

            if (distA > distB && !chemBond.shouldFlip()) {
                return Pair(testSideB, bVec)
            }
            if (distA > distB && chemBond.shouldFlip()) {
                return Pair(testSideA, aVec)
            }
            if (distB > distA && !chemBond.shouldFlip()) {
                return Pair(testSideA, aVec)
            }
            if (distB > distA && chemBond.shouldFlip()) {
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




    fun getSelectedFormula(): String {
        val s = selectionManager.getMolecule() ?: return ""
        return s.getFormulaString()

    }


    private fun buildUIBond(chemBond: ChemMolecule.ChemBond, bondComponents: MutableList<AbstractUIComponent>): UIBond {
        val ui = UIBond(chemBond.bond.order.numeric(), chemBond.midPoint(), chemBond.bond.isAromatic, chemBond.stereo(), bondComponents, selectionManager.isSelected(chemBond))
        return ui
    }

    fun getSelectedWeight(): Double {
        val s = selectionManager.getMolecule() ?: return 0.0
        return s.getMolecularWeight()
    }

    fun getSelectedHybridisation(): String {
        val s = selectionManager.getAtom()
        return s?.atom?.atomTypeName ?: ""
    }

    companion object {
        private const val INTER_BOND_DISTANCE = 6.0
        private const val INTER_DASH_DISTANCE = AtomBondTool.CONNECTION_DISTANCE / 10.0
        private const val DASH_DISTANCE = AtomBondTool.CONNECTION_DISTANCE / 3.0
    }
}