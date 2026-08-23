package uk.co.jcox.molglide.editor.control.tool

import com.sun.org.apache.xpath.internal.operations.Bool
import org.apache.jena.sparql.function.library.evenInteger
import org.checkerframework.checker.units.qual.m
import org.checkerframework.checker.units.qual.mol
import org.joml.GeometryUtils
import org.joml.Vector2d
import org.joml.minus
import uk.co.jcox.molglide.editor.control.ActionManager
import uk.co.jcox.molglide.editor.control.EventContext
import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.SelectionManager
import uk.co.jcox.molglide.editor.control.actions.CompoundAction
import uk.co.jcox.molglide.editor.control.actions.IDataAction
import uk.co.jcox.molglide.editor.control.actions.MoveAtomAction
import uk.co.jcox.molglide.editor.model.ChemAtom
import uk.co.jcox.molglide.editor.model.ChemBond
import uk.co.jcox.molglide.editor.model.IEditorSelectable
import uk.co.jcox.molglide.editor.model.util.AtomPosSnapshot
import java.util.Vector
import javax.swing.SwingUtilities
import javax.vecmath.Point2d
import kotlin.math.PI

/**
 * This tool works by interacting with the Selection Manager's axis aligned bounding selection box
 *
 * 1) The editor panel updates the current selection box through the controller
 *
 * 2) This tool intercepts any click actions
 *
 * 3) If a click (and then a subsequent mouse drag action) takes place when the mouse is
 * selected over an atom/element that was already selected by the AABB, then the user will be allowed
 * to drag the atoms to a new position.
 *
 * Since dragging an atom to a new position is a continuous "dynamic" action, and not something that can be easily
 * described through a simple action do, and action undo, a fake action has to be submitted to the action manager.
 *
 * This pseudo action stores the original position, and then the position after the mouse is released.
 *
 * 4) If a click happens when the mouse is selected over something that was not covered by the AABB
 * then deselect everything and continue on as before!
 */



class SelectTool(actionManager: ActionManager, selectionManager: SelectionManager, editorData: EditorStateData) : Tool(actionManager, selectionManager) {

    private var toolMode: ToolMode = ToolMode.None

    override fun onClick(clickX: Int, clickY: Int, eventContext: EventContext) {
        //First check if the atom should be selected in the batch selection
        val primaryAtom = selectionManager.getAtom()
        if ((eventContext.isShiftDown || eventContext.isCtrlDown) && primaryAtom != null) {
            selectionManager.batchSelection.add(primaryAtom)
        }
        val primaryBond = selectionManager.getBond()
        if ((eventContext.isShiftDown || eventContext.isCtrlDown) && primaryBond != null) {
            selectionManager.batchSelection.add(primaryBond)
        }

        toolMode = getToolMode(clickX, clickY, eventContext)

        if (toolMode == ToolMode.None) {
            selectionManager.clearSelectionBoundingBox()
            return
        }
    }

    override fun onRelease(clickX: Int, clickY: Int, eventContext: EventContext) {
        //On release we have to make the fake action
        val m = toolMode
        when (m) {
            is ToolMode.Dragging -> {
                submitActions(m)
            }
            is ToolMode.None -> {}
        }
//        toolMode = ToolMode.None
    }

    private fun submitActions(m: ToolMode.Dragging) {
        val actionList = mutableListOf<IDataAction>()
        m.posMap.map().forEach { (chemAtom, originalPos) ->
            val currentPos = chemAtom.atom.point2d
            val pseudoAction = MoveAtomAction(chemAtom, currentPos, Point2d(originalPos.x, originalPos.y))
            actionList.add(pseudoAction)
        }
        val actions = actionList.toTypedArray()
        val compoundAction = CompoundAction(*actions)
        actionManager.executeAction(compoundAction)
    }

    override fun onDragMouse(clickX: Int, clickY: Int, dx: Double, dy: Double, eventContext: EventContext) {
        val m = toolMode

        when (m) {
            is ToolMode.Dragging -> handleSelectionDrag(m, clickX, clickY, eventContext)
            is ToolMode.None -> {}
        }

    }
    private fun handleSelectionDrag(m: ToolMode.Dragging, clickX: Int, clickY: Int, eventContext: EventContext) {
        //The user is dragging on an already selected item
        //or a group of items.
        if (!eventContext.isShiftDown) {
            translateSelection(m, clickX, clickY)
        }
        if (eventContext.isShiftDown) {
            rotateSelection(m, clickX, clickY)
        }
    }


    private fun translateSelection(m: ToolMode.Dragging, clickX: Int, clickY: Int) {
        m.posMap.map().keys.forEach{ chemAtom ->
            val originalPos = m.posMap[chemAtom] ?: return
            val newPosX = originalPos.x + (clickX.toDouble() - m.firstClickX)
            val newPosY = originalPos.y + (clickY.toDouble() - m.firstClickY)
            chemAtom.atom.point2d.x = newPosX
            chemAtom.atom.point2d.y = newPosY
        }
    }


    private fun rotateSelection(m: ToolMode.Dragging, clickX: Int, clickY: Int) {
        val currentMouse = Vector2d(clickX.toDouble(), clickY.toDouble())
        val moleculeCentre = m.centre

        val vecToMouse = (currentMouse - moleculeCentre).normalize()
        val randomUpVector = Vector2d(0.0, 1.0)
        val angle = randomUpVector.angle(vecToMouse)

        m.posMap.map().keys.forEach { chemAtom ->
            val originalPos = m.posMap[chemAtom] ?: return
            val newPos = originalPos.rotateAround(angle,  moleculeCentre, Vector2d())
            chemAtom.atom.point2d.x = newPos.x
            chemAtom.atom.point2d.y = newPos.y
        }
    }

    override fun onSuddenMove() {

    }

    private fun getToolMode(clickX: Int, clickY: Int, eventContext: EventContext) : ToolMode {
        //Check if the selected atom was covered in the AABB
        val batchSelection = selectionManager.batchSelection
        val primary = selectionManager.primarySelection
        val mouseOverSelection = selectionManager.isAABBSelected(selectionManager.primarySelection?.selectable)
        if (mouseOverSelection) {
            val atomList = selectionManager.getBatchAtoms()
            return ToolMode.Dragging(clickX, clickY, AtomPosSnapshot(atomList), calcMoleculeCentre(atomList))
        }

        //Check to see if the user has just selected one atom through the primary selection
        //and control or shift is not down
        val atom = selectionManager.getAtom()
        if (atom != null && !(eventContext.isShiftDown || eventContext.isCtrlDown)) {
            val atomList = listOf(atom)
            return ToolMode.Dragging(clickX, clickY, AtomPosSnapshot(atomList), calcMoleculeCentre(atomList))
        }

        selectionManager.clearSelectionBoundingBox()
        return ToolMode.None
    }

    private fun calcMoleculeCentre(chemAtomList: List<ChemAtom>): Vector2d {
        var x = 0.0
        var y = 0.0
        val total = chemAtomList.size
        chemAtomList.forEach { chemAtom ->
            val p = chemAtom.getPos()
            x += p.x
            y += p.y
        }
        x /= total
        y /= total
        return Vector2d(x, y)
    }

    override fun shouldShowAABB(): Boolean {
        return toolMode == ToolMode.None
    }

    override fun isTypeValidPrimarySelection(entity: IEditorSelectable): Boolean {
        return (entity is ChemAtom) or (entity is ChemBond)
    }

    sealed class ToolMode {
        object None: ToolMode()
        class Dragging(val firstClickX: Int, val firstClickY: Int, val posMap: AtomPosSnapshot, val centre: Vector2d) : ToolMode()
    }
}