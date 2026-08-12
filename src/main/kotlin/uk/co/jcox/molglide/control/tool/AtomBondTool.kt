package uk.co.jcox.molglide.control.tool

import com.sun.accessibility.internal.resources.accessibility
import com.sun.org.apache.xpath.internal.operations.Bool
import org.apache.jena.vocabulary.AS.radius
import org.joml.Vector2f
import org.joml.minus
import org.joml.plus
import uk.co.jcox.molglide.control.ActionManager
import uk.co.jcox.molglide.control.AppManager
import uk.co.jcox.molglide.control.ChemMolecule
import uk.co.jcox.molglide.control.EditorStateData
import uk.co.jcox.molglide.control.SelectionManager
import uk.co.jcox.molglide.control.actions.AtomInsertionAction
import uk.co.jcox.molglide.control.actions.CreateMoleculeAction
import uk.co.jcox.molglide.control.actions.IncrementBondOrderAction
import uk.co.jcox.molglide.control.actions.ReplaceAtomAction
import uk.co.jcox.molglide.control.actions.RingCyclisationAction
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class AtomBondTool(val appManager: AppManager, val actionManager: ActionManager,
                   val selectionManager: SelectionManager, val editorData: EditorStateData
) : Tool(actionManager, selectionManager, editorData) {

    var toolMode: Mode = Mode.None

    override fun runUpdates() {

    }

    override fun onDragMouse(clickX: Int, clickY: Int) {
        val currentMode = toolMode
        if (currentMode is Mode.AtomInsertionDragging) {
            handleNewAtomDragging(currentMode, clickX, clickY)
        }
    }

    private fun handleNewAtomDragging(mode: Mode.AtomInsertionDragging, clickX: Int, clickY: Int) {
        //Need the dragging atom to follow the mouse around
        //at fixed, chemically sensical bond angles

        //Find the position of the original (anchor) atom
        val anchorPos = mode.insertedTo.atom.point2d

        val calculatedNewPos = closestPointToCircleCircumference(Vector2f(anchorPos.x.toFloat(), anchorPos.y.toFloat()), Vector2f(clickX.toFloat(), clickY.toFloat()), CONNECTION_DISTANCE.toFloat())
        mode.draggingAtom.atom.point2d.x = calculatedNewPos.x.toDouble()
        mode.draggingAtom.atom.point2d.y = calculatedNewPos.y.toDouble()

        //Check to see if any trailing groups should be automatically moved
        autoMoveTrailingGroup(mode)
        checkBondOrderChange(mode)
    }

    private fun autoMoveTrailingGroup(mode: Mode.AtomInsertionDragging) {
        val anchor = mode.insertedTo
        val dragging = mode.draggingAtom

        autoMoveGroup(anchor, dragging)
        autoMoveGroup(dragging, anchor)
    }

    /**
     * Automatically adjust one atom's trailing group position, depending on its position to
     * another atom
     * @param checkAgainst this atom will not be affected by this function
     * @param applier this method will change this atom's trailing group mode
     *
     * This function works by testing the two main positions a trailing group can reside
     * either left or right of the master atom.
     * This method tests both positions to see which has the longest distance away
     */
    private fun autoMoveGroup(checkAgainst: ChemMolecule.ChemAtom, applier: ChemMolecule.ChemAtom) {
        val leftTest = ChemMolecule.TrailingGroupPosition.LEFT.vec + applier.getPos()
        val rightTest = ChemMolecule.TrailingGroupPosition.RIGHT.vec + applier.getPos()

        val leftDistance = checkAgainst.getPos().distance(leftTest)
        val rightDistance = checkAgainst.getPos().distance(rightTest)

        if (leftDistance == rightDistance) {
            return
        }

        if (rightDistance > leftDistance) {
            applier.setTrailPos(ChemMolecule.TrailingGroupPosition.RIGHT)
        } else {
            applier.setTrailPos(ChemMolecule.TrailingGroupPosition.LEFT)

        }
    }


    /**
     * When in dragging mode the user is free to move the newly inserted atom wherever they want
     * Should this atom align with an already existing bond, then we should attempt to increase the bond order of that bond
     *
     * If the user pulls away again, then we should undo that increasing of the bond order
     * However, additionally, this tool (class) needs to be restored to how it was to allow
     * the user to move the freely created atom
     *
     * Using the same method it is possible to detect cyclisation actions
     *
     * Internal Undo/Redo is controlled by the ActionManager
     */
    private fun checkBondOrderChange(draggingMode: Mode.AtomInsertionDragging) {
        val draggingPos = draggingMode.draggingAtom.getPos()
        val molecule = draggingMode.insertedTo.molecule

        //Find an atom that is overlapping
        val overlap = molecule.atoms().toList().find {
            draggingPos.equals(it.getPos(), 0.25) && it.atom != draggingMode.draggingAtom.atom && it.atom != draggingMode.insertedTo.atom
        }


        if (overlap != null && draggingMode.allowBondChanges) {
            actionManager.undoLastAction()
            draggingMode.allowBondChanges = false
            //Then we have found an overlapping atom
            //And the tool allows bond changes
            //So undo the insert, and instead update the bond order of the common bond between the two atoms
            val commonBond = molecule.findBond(overlap, draggingMode.insertedTo)
            //If the bond exists, then update order
            if (commonBond != null) {
                val action = IncrementBondOrderAction(commonBond)
                actionManager.executeAction(action)
                return
            }
            //Otherwise, there was no common bond between the atoms, and we need to instead perform
            //a cyclisation action
            handleRingCyclisationAction(draggingMode.insertedTo, overlap)
            return
        }

        //If no overlap was found, and we cannot make bond changes
        //It means the user has pulled away from an atom, after previously being aligned with one
        //Therefore undo everything, and restore normal dragging mode
        if (overlap == null && !draggingMode.allowBondChanges) {
            actionManager.undoLastAction()
            setupDraggingAtom(Mode.PostReplacement(draggingMode.insertedTo))
        }
    }

    private fun handleRingCyclisationAction(anchorAtom: ChemMolecule.ChemAtom, overlap: ChemMolecule.ChemAtom) {
            val action = RingCyclisationAction(anchorAtom, overlap)
            actionManager.executeAction(action)
    }



    private fun closestPointToCircleCircumference(circleCentre: org.joml.Vector2f, randomPoint: org.joml.Vector2f, radius: Float) : org.joml.Vector2f {
        val directionVec = randomPoint - circleCentre
        val angle = org.joml.Vector2f(1.0f, 0.0f).angle(directionVec)

        val refinedAngle: Float = COMMON_ANGLES.minBy { abs(Math.toRadians(it.toDouble()) - angle) }

        val refinedAngeRad = Math.toRadians(refinedAngle.toDouble())

        val x = circleCentre.x + radius * cos(refinedAngeRad)
        val y = circleCentre.y + radius * sin(refinedAngeRad)

        return org.joml.Vector2f(x.toFloat(), y.toFloat())
    }

    override fun onSuddenMove() {
        val currentMode = toolMode

        if (currentMode is Mode.PostReplacement) {
            //Convert to insertion
            actionManager.undoLastAction()
            setupDraggingAtom(currentMode)
        }
    }

    private fun setupDraggingAtom(currentMode: Mode.PostReplacement) {
        val atomInsertionAction = AtomInsertionAction(appManager.editMode.symbol, currentMode.insertTo, mouseX, mouseY)
        actionManager.executeAction(atomInsertionAction)
        atomInsertionAction.newAtom?.let { toolMode = Mode.AtomInsertionDragging(it, currentMode.insertTo, true) }
    }

    override fun onClick(clickX: Int, clickY: Int) {
        toolMode = getToolMode(clickX, clickY)

        when (val mode = toolMode) {
            is Mode.MolCreation -> createNewMolecule(mode)
            is Mode.AtomReplacement -> replaceAtom(mode)
            is Mode.PostReplacement -> {}
            is Mode.AtomInsertionDragging -> {}
            Mode.None -> {}

        }
    }

    private fun createNewMolecule(molCreation: Mode.MolCreation) {
        val atomCreationAction = CreateMoleculeAction(molCreation.xPos, molCreation.yPos, appManager.editMode.symbol)
        actionManager.executeAction(atomCreationAction)
    }

    private fun replaceAtom(atomInsertion: Mode.AtomReplacement) {
        val atomReplacementAction = ReplaceAtomAction(atomInsertion.replace, appManager.editMode.symbol)
        actionManager.executeAction(atomReplacementAction)
        toolMode = Mode.PostReplacement(atomInsertion.replace)
    }

    override fun onRelease(clickX: Int, clickY: Int) {
        toolMode = Mode.None
    }

    private fun getToolMode(clickX: Int, clickY: Int) : Mode {
        val selection = selectionManager.primary

        //If the selection is active (as in the user is selecting an atom)
        //Any subsequent click should replace the atom selected with the active atom from the toolbox
        if (selection is SelectionManager.Type.Active) {
            return Mode.AtomReplacement(selection.chemAtom)
        }

        //If nothing is selected, assume we are creating a new atom
        return Mode.MolCreation(clickX, clickY)
    }

    sealed class Mode {
        object None: Mode()
        class MolCreation(val xPos: Int, val yPos: Int) : Mode()
        class AtomReplacement(val replace: ChemMolecule.ChemAtom) : Mode()

        //This mode is strictly used only in the sudden move method
        //it is swithced on after a replacement action, to check if the user actually
        //intended to add a bond
        class PostReplacement(val insertTo: ChemMolecule.ChemAtom) : Mode()


        //This mode is strictly used only in the drag method after the
        //insertion has taken place. This allows the user to drag around and decide
        //on the new bond angle
        //The atom that was just added becomes dragging, and this was inserted into the already existing atom
        class AtomInsertionDragging(val draggingAtom: ChemMolecule.ChemAtom, val insertedTo: ChemMolecule.ChemAtom, var allowBondChanges: Boolean) : Mode()
    }

    companion object {

        private const val CONNECTION_DISTANCE = 60

        private val COMMON_ANGLES = listOf<Float>(
            //Cardinal directions
            0.0f, 90.0f, -90.0f, 180.0f, -180.0f,

            //Semi Cardinal directions
            45.0f, 135.0f, -45.0f, -135.0f,

            //Odd angles - For triangles
            30.0f, -30.0f, 60.0f, -60.0f, 120.0f, -120.0f, 150.0f, -150.0f,

            //For Pentagons
            108.0f, -108.0f, 72.0f, -72.0f, 36.0f, -36.0f, 126.0f, -126.0f, 144.0f, -144.0f,
            18.0f, -18.0f, 162.0f, -162.0f, 126.0f, -126.0f, 36.0f, -36.0f, 54.0f, -54.0f,
        )
    }
}