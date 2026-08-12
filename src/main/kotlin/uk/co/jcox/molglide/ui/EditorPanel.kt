package uk.co.jcox.molglide.ui

import com.github.jsonldjava.shaded.com.google.common.math.IntMath.pow
import org.joml.Vector2d
import org.joml.minus
import org.joml.plus
import org.joml.times
import org.xmlcml.euclid.Vector2
import uk.co.jcox.molglide.control.ChemMolecule
import uk.co.jcox.molglide.control.EditorStateController
import uk.co.jcox.molglide.control.UIAtom
import java.awt.BasicStroke
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.RenderingHints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.awt.font.TextAttribute
import java.text.AttributedString
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.Timer
import javax.swing.UIManager
import kotlin.math.max
import kotlin.math.sqrt

class EditorPanel(val dataController: EditorStateController) : JPanel() {

    private var cameraX: Double = 0.0
    private var cameraY: Double = 0.0
    private var cameraZoom: Float = 1.0f
        set(value) {field = max(1.0f, value)}
    private var lastMousePos: Point = Point()

    init {
        val timer = Timer(16) {
            repaint()
            val world = screenToWorld(lastMousePos)
            dataController.update(world.x, world.y)
        }
        timer.start()

        val mouseListener = MouseEvents()
        addMouseListener(mouseListener)
        addMouseMotionListener(mouseListener)
        addMouseWheelListener(mouseListener)

    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        val g2d = preparePainter(g)
        paintAtoms(g2d)
        paintBonds(g2d)
    }


    private fun preparePainter(graphics: Graphics?) : Graphics2D {
        val g2d = graphics as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g2d.font = Font("Liberation Serif", Font.PLAIN, -5)
        g2d.font = g2d.font.deriveFont(Font.PLAIN, UNMODDED_TEXT_SIZE * cameraZoom)

        g2d.translate(cameraX, cameraY)


        return g2d
    }

    private fun paintAtoms(g2d: Graphics2D) {
        val atomsToPaint = dataController.getUIAtoms()
        atomsToPaint.forEach { ui ->
            val x = ui.posX * cameraZoom
            val y = ui.posY * cameraZoom

            val metrics = getMasterMetrics(g2d, ui, x, y)
            paintAtomSelection(g2d, ui, metrics)
            if (ui.visible) {
                paintMasterAtom(g2d, ui, metrics)
                if (ui.trailGroup != "") {
                    paintTrailGroup(g2d, ui, x, y, metrics)
                }
            }

        }
    }


    private fun getMasterMetrics(g2d: Graphics2D, uiAtom: UIAtom, x: Double, y: Double): MasterAtomMetric {
        val textWidth = g2d.fontMetrics.stringWidth(uiAtom.element)
        val textHeight = g2d.fontMetrics.ascent - g2d.fontMetrics.descent

        val centreTextWidth = x - textWidth / 2
        val centreBoxWidth = x - textWidth
        val centreTextHeight = y + textHeight / 2
        val centreBoxHeight = y - g2d.fontMetrics.height / 2
        return MasterAtomMetric(centreTextWidth, centreTextHeight, centreBoxWidth, centreBoxHeight, textWidth, textHeight)
    }

    private fun paintAtomSelection(g2d: Graphics2D, uiAtom: UIAtom, m: MasterAtomMetric) {
        if (dataController.checkSelected(uiAtom)) {
            val oldColour = g2d.color
            val newColour = UIManager.getColor("Component.accentColor")
            g2d.color = newColour
            g2d.fillRoundRect((m.centreBoxWidth).toInt(),
                (m.centreBoxHeight).toInt(),
                (m.textWidth * 2),
                (m.textWidth * 2), m.textWidth, m.textWidth)
            g2d.color = oldColour
        }
    }

    private fun paintMasterAtom(g2d: Graphics2D, uiAtom: UIAtom,  m: MasterAtomMetric) {
        g2d.drawString(uiAtom.element, m.centreTextWidth.toInt(), m.centreTextHeight.toInt())
    }

    private fun paintTrailGroup(g2d: Graphics2D, uiAtom: UIAtom, x: Double, y: Double, m: MasterAtomMetric) {

        val s = getStartingPos(g2d,x, y, m, uiAtom)
        val startX = s.x
        val startY = s.y

        val attString = AttributedString(uiAtom.trailGroup)
        attString.addAttribute(TextAttribute.FAMILY, g2d.font.family)
        attString.addAttribute(TextAttribute.SIZE, g2d.font.size)

        val range = getSubscriptRange(uiAtom.trailGroup)
        range.forEach {
            attString.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUB,  it, it + 1)
        }
        g2d.drawString(attString.iterator, startX.toFloat(), startY.toFloat())
    }

    private fun getStartingPos(g2d: Graphics2D, x: Double, y: Double, m: MasterAtomMetric, uiAtom: UIAtom) : Vector2d {
        val startX = x + m.textWidth / 2
        val startY = y + m.textHeight / 2

        if (uiAtom.trailGroupPos == ChemMolecule.TrailingGroupPosition.RIGHT) {
            return Vector2d(startX, startY)
        }
        if (uiAtom.trailGroupPos == ChemMolecule.TrailingGroupPosition.LEFT) {
            val trail = uiAtom.trailGroup
            val textWidth = g2d.fontMetrics.stringWidth(trail)
            return Vector2d(startX - textWidth - m.textWidth, startY)
        }


        return Vector2d(startX, startY)
    }

    private fun getSubscriptRange(trailGroup: String) : List<Int> {
        val list = ArrayList<Int>()
        trailGroup.forEachIndexed { index, ch ->
            if (ch.isDigit()) {
                list.add(index)
            }
        }
        return list
    }

    private fun paintBonds(g2d: Graphics2D) {
        g2d.stroke = BasicStroke(LINE_STROKE * cameraZoom, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        val bondsToPaint = dataController.getBondsToDraw()
        bondsToPaint.forEach { bondUI ->
            val startX = bondUI.startX * cameraZoom
            val startY = bondUI.startY * cameraZoom
            val endX = bondUI.endX * cameraZoom
            val endY = bondUI.endY * cameraZoom

            g2d.drawLine(startX.toInt(), startY.toInt(), endX.toInt(), endY.toInt())
        }
    }
    private fun screenToWorld(screen: Point) : Point {
        var x: Double = screen.x.toDouble()
        var y: Double = screen.y.toDouble()
        x -= cameraX
        y -= cameraY
        x /= cameraZoom
        y /= cameraZoom
        return Point(x.toInt(), y.toInt())
    }

    inner class MouseEvents: MouseAdapter() {

        private var offsetX: Int = 0
        private var offsetY: Int = 0

        override fun mouseClicked(e: MouseEvent?) {

        }

        override fun mousePressed(e: MouseEvent?) {
            if (e == null) {
                return
            }
            val point = screenToWorld(e.point)
            if (SwingUtilities.isLeftMouseButton(e)) {
                dataController.handleMouseClick(point.x, point.y)
            }
        }

        override fun mouseReleased(e: MouseEvent?) {
            if (e == null) {
                return
            }
            val point = screenToWorld(e.point)
            dataController.handleMouseRelease(point.x, point.y)
        }

        override fun mouseEntered(e: MouseEvent?) {

        }

        override fun mouseExited(e: MouseEvent?) {

        }

        override fun mouseWheelMoved(e: MouseWheelEvent?) {
            if (e == null) {
                return
            }
            if(e.isControlDown) {
                cameraZoom -= e.wheelRotation * MOUSE_SENSE_ZOOM
            }
        }

        override fun mouseDragged(e: MouseEvent?) {
            if (e == null) {
                return
            }

            val worldPoint = screenToWorld(e.point)
            dataController.handleMouseDrag(worldPoint.x, worldPoint.y)

            updateMouse(e)
            if (SwingUtilities.isMiddleMouseButton(e)) {
                cameraX += offsetX * MOUSE_SENSE
                cameraY += offsetY * MOUSE_SENSE

            }
        }

        override fun mouseMoved(e: MouseEvent?) {
            updateMouse(e)
            dataController.nowActive()
        }

        private fun updateMouse(e: MouseEvent?) {
            if (e == null) {
                return
            }
            val currentPos = e.point
            offsetX = currentPos.x - lastMousePos.x
            offsetY = currentPos.y - lastMousePos.y
            lastMousePos.x = currentPos.x
            lastMousePos.y = currentPos.y
            if (sqrt((pow(offsetX, 2) + pow(offsetY, 2)).toDouble()) >= SIG_MOUSE_DELTA) {
                dataController.handleSuddenMouseMove()
            }
        }
    }
    companion object {
        private const val MOUSE_SENSE = 2.0
        private const val MOUSE_SENSE_ZOOM = 0.5f
        private const val UNMODDED_TEXT_SIZE = 32.0f
        private const val SIG_MOUSE_DELTA = 2.0f
        private const val LINE_STROKE = 3.0f
    }
}