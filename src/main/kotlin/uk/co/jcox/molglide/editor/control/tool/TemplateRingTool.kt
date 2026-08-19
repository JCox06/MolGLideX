package uk.co.jcox.molglide.editor.control.tool

import org.joml.Vector2d
import org.joml.minus
import uk.co.jcox.molglide.editor.control.ActionManager
import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.SelectionManager
import uk.co.jcox.molglide.editor.control.actions.RingCreatorAction
import uk.co.jcox.molglide.IMainAppData
import javax.vecmath.Point2d
import kotlin.math.roundToInt

class TemplateRingTool(val globalContext: IMainAppData, actionManager: ActionManager, selectionManager: SelectionManager) : Tool(actionManager, selectionManager) {

    private var toolMode: Mode = Mode.None

    override fun onClick(clickX: Int, clickY: Int) {
        val primary = selectionManager.primarySelection

        if (primary is SelectionManager.Type.None) {
            addIsolatedRing(clickX, clickY)
        }
    }

    private fun addIsolatedRing(centreX: Int, centreY: Int) {
        //Add the ring as an isolated ring since the user is not selecting anything
        val action = RingCreatorAction(centreX, centreY, globalContext.getEditMode())
        actionManager.executeAction(action)
        val c = action.getRingCentre()
        toolMode = Mode.Rotate(action.placedRing, c.x, c.y, 0)
    }

    override fun onRelease(clickX: Int, clickY: Int) {
        toolMode = Mode.None
    }

    override fun onDragMouse(clickX: Int, clickY: Int, dx: Double, dy: Double) {
        val currentMode = toolMode
        if (currentMode is Mode.Rotate) {
            rotateRingAngle(clickX, clickY, currentMode)
        }
    }



    //This is so annoying
    //todo Fix this
    //I can't figure out why rotating rings after placing a new one is not working correctly
    //It seems the rotations are about by a few degrees, which is strange considering nearest60 produces
    //perfect integer values of rotation.
    //
    //And the centre of rotation is the centre of the ring, (something which I have confirmed through averaging
    //the positions of the bonds) which rules out CDK changing it for whatever reason
    private fun rotateRingAngle(clickX: Int, clickY: Int, currentMode: Mode.Rotate) {

        val ringCentre = Vector2d(currentMode.ringCentreX, currentMode.ringCentreY)
        val currentMouse = Vector2d(clickX.toDouble(), clickY.toDouble())

        val vecToMouse = (currentMouse - ringCentre).normalize()
        val randomUpVector = Vector2d(0.0, 1.0)

        val angle = randomUpVector.angle(vecToMouse)
        val angleDeg = Math.toDegrees(angle)
        val angleIncr = 360 / globalContext.getEditMode().ringSize

        val nearest60: Int = ((angleDeg / angleIncr.toDouble()).roundToInt() * angleIncr)

        if (currentMode.firstRun) {
            currentMode.lastAngle = nearest60
            currentMode.firstRun = false
        }

        if (nearest60 != currentMode.lastAngle) {
            val angleDiff = nearest60 - currentMode.lastAngle
            currentMode.inserted.atoms().forEach { chemAtom ->
                val pos = chemAtom.getPos()
                pos.rotateAround(angleDiff.toDouble(), Vector2d(currentMode.ringCentreX, currentMode.ringCentreY))
                chemAtom.atom.point2d = Point2d(pos.x, pos.y)
            }
        }
        currentMode.lastAngle = nearest60

    }


    override fun onSuddenMove() {

    }


    private sealed class Mode {
        object None : Mode()
        class Rotate(val inserted: ChemMolecule, val ringCentreX: Double, val ringCentreY: Double, var lastAngle: Int, var firstRun: Boolean = true): Mode()
    }
}