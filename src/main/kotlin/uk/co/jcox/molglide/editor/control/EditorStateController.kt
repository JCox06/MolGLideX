package uk.co.jcox.molglide.editor.control

import com.github.jsonldjava.shaded.com.google.common.math.IntMath.pow
import uk.co.jcox.molglide.EditMode
import uk.co.jcox.molglide.editor.control.tool.AtomBondTool
import uk.co.jcox.molglide.editor.control.tool.FormalChargeLonePairTool
import uk.co.jcox.molglide.editor.control.tool.SelectTool
import uk.co.jcox.molglide.editor.control.tool.TemplateRingTool
import uk.co.jcox.molglide.editor.control.tool.Tool
import uk.co.jcox.molglide.IMainAppData
import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.ui.EditorPanel
import uk.co.jcox.molglide.editor.ui.EditorPanel.Companion.MOUSE_SENSE
import uk.co.jcox.molglide.editor.ui.EditorPanel.Companion.MOUSE_SENSE_ZOOM
import uk.co.jcox.molglide.editor.ui.EditorPanel.Companion.SIG_MOUSE_DELTA
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import javax.swing.SwingUtilities
import javax.swing.Timer
import kotlin.math.sqrt

class EditorStateController (
    private val globalContext: IMainAppData,
    private val stateData : EditorStateData,
    private val editorPanel: EditorPanel,
) {
    val actionManager: ActionManager = ActionManager(stateData) { dataHasChanged() }
    private var currentTool: Tool = AtomBondTool(globalContext, actionManager, stateData.selectionManager)

    init {
        val panelMouseEvents = PanelMouseEvents()
        editorPanel.addMouseMotionListener(panelMouseEvents)
        editorPanel.addMouseListener(panelMouseEvents)
        editorPanel.addMouseWheelListener(panelMouseEvents)

        val timer = Timer(16) {
            editorPanel.refreshEditor()
            val world = screenToWorld(stateData.mouseX.toDouble(), stateData.mouseY.toDouble())
            update(world.x, world.y)

            stateData.uiDataBuilder.rebuild(false)
        }
        timer.start()
    }

    private fun handleMouseClick(clickX: Int, clickY: Int) {
        prepareTool()
        currentTool.onClick(clickX, clickY)
    }

    private fun handleMouseRelease(clickX: Int, clickY: Int) {
        currentTool.onRelease(clickX, clickY)
    }

    private fun handleSuddenMouseMove() {
        currentTool.onSuddenMove()
    }

    private fun handleMouseDrag(mouseX: Int, mouseY: Int, dx: Double, dy: Double) {
        currentTool.onDragMouse(mouseX, mouseY, dx, dy)
    }

    private fun translateCameraPos(x: Double, y: Double) {
        stateData.cameraX += x
        stateData.cameraY += y
    }

    fun update(worldX: Int, worldY: Int) {
        currentTool.updateMouseWorld(worldX, worldY)
        stateData.selectionManager.updatePrimarySelection(stateData, worldX, worldY)
    }

    private fun prepareTool() {
        if (globalContext.getEditMode().type == EditMode.ToolType.ATOM_INSERT) {
            currentTool = AtomBondTool(globalContext, actionManager, stateData.selectionManager)
            stateData.selectionManager.clearSelectionBoundingBox()
        }
        if (globalContext.getEditMode().type == EditMode.ToolType.RING_INSERT) {
            currentTool = TemplateRingTool(globalContext, actionManager, stateData.selectionManager)
            stateData.selectionManager.clearSelectionBoundingBox()
        }
        if (globalContext.getEditMode().type == EditMode.ToolType.SELECT_TOOL) {
            currentTool = SelectTool(actionManager, stateData.selectionManager, stateData)
        }

        if (globalContext.getEditMode().type == EditMode.ToolType.FORMAL_CHARGE) {
            currentTool = FormalChargeLonePairTool(globalContext, actionManager, stateData.selectionManager)
        }
    }

    private fun screenToWorld(screen: Point): Point {
        val x: Double = screen.x.toDouble()
        val y: Double = screen.y.toDouble()
        return screenToWorld(x, y)
    }

    private fun screenToWorld(screenX: Double, screenY: Double) : Point {
        var x = screenX
        var y = screenY
        val camX = stateData.cameraX
        val camY = stateData.cameraY
        val camZoom = stateData.cameraZoom
        x -= camX
        y -= camY
        x /= camZoom
        y /= camZoom
        return Point(x.toInt(), y.toInt())
    }

    //Rebuilds the entire UI
    private fun dataHasChanged() {
        rebuildEntireUI()
    }

    private fun rebuildEntireUI() {
        stateData.uiDataBuilder.rebuild(true)
    }

    inner class PanelMouseEvents: MouseAdapter() {

        private var offsetX: Int = 0
        private var offsetY: Int = 0

        override fun mousePressed(e: MouseEvent?) {
            if (e == null || stateData.pauseEvents) {
                return
            }
            val world = screenToWorld(e.point)
            if (SwingUtilities.isLeftMouseButton(e)) {
                handleMouseClick(world.x, world.y)
            }
            stateData.transientBoxSelectStartX = world.x
            stateData.transientBoxSelectStartY = world.y
        }

        override fun mouseReleased(e: MouseEvent?) {
            if (e == null || stateData.pauseEvents) {
                return
            }
            val point = screenToWorld(e.point)
            handleMouseRelease(point.x, point.y)

            stateData.transientBoxSelectStartX = 0
            stateData.transientBoxSelectStartY = 0
            stateData.transientBoxSelectAdvY = 0
            stateData.transientBoxSelectAdvX = 0
        }

        override fun mouseWheelMoved(e: MouseWheelEvent?) {
            if (e == null || stateData.pauseEvents) {
                return
            }

            if (e.isControlDown) {
                stateData.cameraZoom -= e.wheelRotation * MOUSE_SENSE_ZOOM
            }
        }

        override fun mouseDragged(e: MouseEvent?) {
            if (e == null || stateData.pauseEvents) {
                return
            }
            updateMouse(e)

            val world = screenToWorld(e.point)
            val moveX = offsetX * MOUSE_SENSE
            val moveY = offsetY * MOUSE_SENSE
            handleMouseDrag(world.x, world.y, moveX, moveY)

            if (SwingUtilities.isMiddleMouseButton(e)) {
                translateCameraPos(moveX, moveY)
            }

            if (stateData.canSelectBox && SwingUtilities.isLeftMouseButton(e) && currentTool is SelectTool && (currentTool as SelectTool).shouldShowAABB()) {
                stateData.transientBoxSelectAdvX = world.x - stateData.transientBoxSelectStartX
                stateData.transientBoxSelectAdvY = world.y - stateData.transientBoxSelectStartY
                stateData.selectionManager.updateSelectionBoundingBox(stateData, stateData.transientBoxSelectStartX, stateData.transientBoxSelectStartY, world.x, world.y)
            }
        }

        override fun mouseMoved(e: MouseEvent?) {
            if (e == null || stateData.pauseEvents) {
                return
            }
            updateMouse(e)
        }

        private fun updateMouse(e: MouseEvent) {
            if (stateData.pauseEvents) {
                return
            }
            val currentPos = e.point
            offsetX = currentPos.x - stateData.getLastMouseX()
            offsetY = currentPos.y - stateData.getLastMouseY()
            stateData.mouseX = currentPos.x
            stateData.mouseY = currentPos.y

            if (sqrt((pow(offsetX, 2) + pow(offsetY, 2)).toDouble()) >= SIG_MOUSE_DELTA) {
                handleSuddenMouseMove()
            }
        }
    }
}