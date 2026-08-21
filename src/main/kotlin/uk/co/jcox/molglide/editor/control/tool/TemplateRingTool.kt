package uk.co.jcox.molglide.editor.control.tool

import org.joml.Vector2d
import org.joml.minus
import org.openscience.cdk.geometry.GeometryUtil
import uk.co.jcox.molglide.editor.control.ActionManager
import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.SelectionManager
import uk.co.jcox.molglide.editor.control.actions.RingCreatorAction
import uk.co.jcox.molglide.IMainAppData
import uk.co.jcox.molglide.editor.control.EventContext
import uk.co.jcox.molglide.editor.model.util.AtomPosSnapshot
import javax.vecmath.Point2d
import kotlin.math.round
import kotlin.math.roundToInt

class TemplateRingTool(val globalContext: IMainAppData, actionManager: ActionManager, selectionManager: SelectionManager) : Tool(actionManager, selectionManager) {

    private var toolMode: Mode = Mode.None

    override fun onClick(clickX: Int, clickY: Int, eventContext: EventContext) {
        val primary = selectionManager.primarySelection

        if (primary == null) {
            addIsolatedRing(clickX, clickY)
        }
    }

    private fun addIsolatedRing(centreX: Int, centreY: Int) {
        //Add the ring as an isolated ring since the user is not selecting anything
        val action = RingCreatorAction(centreX, centreY, globalContext.getEditMode())
        actionManager.executeAction(action)
        val c = action.getRingCentre()
        toolMode = Mode.Rotate(action.placedRing, c.x, c.y, AtomPosSnapshot.ofMolecule(action.placedRing))
        action.placedRing.setTransient(true)
    }

    override fun onRelease(clickX: Int, clickY: Int, eventContext: EventContext) {
        val m = toolMode
        if (m is Mode.Rotate) {
            m.inserted.setTransient(false)
            m.inserted.calculateAtomProperties()
        }
        toolMode = Mode.None
    }

    override fun onDragMouse(clickX: Int, clickY: Int, dx: Double, dy: Double, eventContext: EventContext) {
        val currentMode = toolMode
        if (currentMode is Mode.Rotate) {
            rotateRingAngle(clickX, clickY, currentMode)
        }
    }

    private fun rotateRingAngle(clickX: Int, clickY: Int, currentMode: Mode.Rotate) {

        val ringCentre = Vector2d(currentMode.ringCentreX, currentMode.ringCentreY)
        val currentMouse = Vector2d(clickX.toDouble(), clickY.toDouble())

        val vecToMouse = (currentMouse - ringCentre).normalize()
        val randomUpVector = Vector2d(0.0, 1.0)

        val angle = randomUpVector.angle(vecToMouse)
        val angleIncr = (Math.PI * 0.5) / globalContext.getEditMode().ringSize
        var nearestSnap: Double = round((angle / angleIncr)) * angleIncr

        currentMode.inserted.atoms().forEach { chemAtom ->
            val originalPos = currentMode.posMap[chemAtom]
            val newPos = originalPos.rotateAround(nearestSnap, currentMode.ringCentreX, currentMode.ringCentreY, Vector2d())
            chemAtom.atom.point2d.x = newPos.x
            chemAtom.atom.point2d.y = newPos.y
        }
    }


    override fun onSuddenMove() {

    }


    private sealed class Mode {
        object None : Mode()
        class Rotate(val inserted: ChemMolecule, val ringCentreX: Double, val ringCentreY: Double, val posMap: AtomPosSnapshot): Mode()
    }
}