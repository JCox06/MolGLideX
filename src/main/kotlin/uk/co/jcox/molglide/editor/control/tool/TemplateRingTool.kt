package uk.co.jcox.molglide.editor.control.tool

import org.joml.Vector2d
import org.joml.minus
import uk.co.jcox.molglide.IMainAppData
import uk.co.jcox.molglide.editor.control.ActionManager
import uk.co.jcox.molglide.editor.control.EventContext
import uk.co.jcox.molglide.editor.control.actions.RingCreatorAction
import uk.co.jcox.molglide.editor.model.SelectionManager
import uk.co.jcox.molglide.editor.model.chemengine.MgxAtom
import uk.co.jcox.molglide.editor.model.chemengine.MgxBond
import uk.co.jcox.molglide.editor.model.chemengine.MgxMolecule
import uk.co.jcox.molglide.editor.model.util.EditorPositionSnapshot
import kotlin.math.round

class TemplateRingTool(val globalContext: IMainAppData, actionManager: ActionManager, selectionManager: SelectionManager) : Tool(actionManager, selectionManager) {

    private var toolMode: Mode = Mode.None

    override fun onClick(clickX: Int, clickY: Int, eventContext: EventContext) {
        val primary = selectionManager.primarySelection

        if (primary == null) {
            addIsolatedRing(clickX, clickY)
            return
        }
        val bond = selectionManager.getBond()
        val atom = selectionManager.getAtom()
        if (atom != null) {
            addCommonAtomRing(atom)
            return
        }
        if (bond != null) {
            addCommonBondRing(bond)
        }
    }

    private fun addIsolatedRing(centreX: Int, centreY: Int) {
        //Add the ring as an isolated ring since the user is not selecting anything
        val action = RingCreatorAction(centreX, centreY, globalContext.getEditMode())
        actionManager.executeAction(action)
        val c = action.mgxMolecule.getSpatialCentre()
        toolMode = Mode.Rotate(action.mgxMolecule, c.x, c.y, EditorPositionSnapshot.ofMolecule(action.mgxMolecule))
        action.mgxMolecule.bulkSetTransient(true)
    }

    private fun addCommonBondRing(mgxBong: MgxBond) {
        //todo the following two methods will need to each have a corresponding action
        //The action will then use MgxMolecule#getTemplateBuilder() to build each of the rings
        TODO("No implementation for fused rings")
    }

    private fun addCommonAtomRing(mgxAtom: MgxAtom) {
        TODO("No implementation for spiro rings")
    }

    override fun onRelease(clickX: Int, clickY: Int, eventContext: EventContext) {
        val m = toolMode
        if (m is Mode.Rotate) {
            m.inserted.bulkSetTransient(false)
            m.inserted.calculateChemData()
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
        val nearestSnap: Double = round((angle / angleIncr)) * angleIncr

        currentMode.posMap.rotateCoordinates(currentMode.ringCentreX, currentMode.ringCentreY, nearestSnap)
    }


    override fun isTypeValidPrimarySelection(selectionContext: SelectionManager.SelectionInfo): Boolean {
        val entity = selectionContext.selectable
        return (entity is MgxAtom) || (entity is MgxBond)
    }

    override fun onSuddenMove() {

    }


    private sealed class Mode {
        object None : Mode()
        class Rotate(val inserted: MgxMolecule, val ringCentreX: Double, val ringCentreY: Double, val posMap: EditorPositionSnapshot): Mode()
    }
}