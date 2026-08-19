package uk.co.jcox.molglide.ui

import org.joml.Vector2d
import uk.co.jcox.molglide.MolGLideUtils
import uk.co.jcox.molglide.control.ChemMolecule
import uk.co.jcox.molglide.control.IDataModelUI
import uk.co.jcox.molglide.control.UIAtom
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.font.TextAttribute
import java.text.AttributedString
import javax.swing.JPanel
import kotlin.math.absoluteValue


class EditorPanel(private val uiData: IDataModelUI) : JPanel() {


    /**
     * Called by the controller when there is new UI information
     * that needs to be painted
     */
    fun refreshEditor() {
        repaint()
    }

    fun paintEditor(g2d: Graphics2D, drawTransients: Boolean = true, onlyDrawSelected: Boolean = false) {
        preparePainter(g2d)

        paintGenericSelectionBox(g2d)
        paintBondSelection(g2d)
        paintAtoms(g2d, drawTransients)
        paintLines(g2d)
        paintTriangles(g2d)
    }

    protected override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        val g2d = g as? Graphics2D ?: return
        paintEditor(g2d)
    }

    private fun preparePainter(g2d: Graphics2D) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g2d.font = Font("Liberation Serif", Font.PLAIN, -5)
        g2d.font = g2d.font.deriveFont(Font.PLAIN, getFontSize())


        g2d.translate(uiData.cameraX(), uiData.cameraY())
    }


    private fun paintAtoms(g2d: Graphics2D, paintTransients: Boolean) {
        val atomsToPaint = uiData.getAtomData()
        atomsToPaint.forEach { ui ->
            val x = ui.posX * uiData.cameraZoom()
            val y = ui.posY * uiData.cameraZoom()

            val metrics = getMasterMetrics(g2d, ui, x, y)

            //Draw the selection marker
            if (paintTransients && ui.selected) paintAtomTextBoxBorder(g2d, ui, metrics, MolGLideUtils.getAccentColour(), true)

            //Check if error
            val red = Color.red
            if (paintTransients && !ui.ignoreErrors && ui.hasErrors) paintAtomTextBoxBorder(g2d, ui, metrics, red, false)

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

    //Is used for the atom selection marker, but also for any errors that may arise
    private fun paintAtomTextBoxBorder(g2d: Graphics2D, uiAtom: UIAtom, m: MasterAtomMetric, color: Color, shouldFill: Boolean) {
        val oldColour = g2d.color
        val newColour = color
        g2d.color = newColour
        if (shouldFill) {
            g2d.fillRoundRect((m.centreBoxWidth).toInt(),
                (m.centreBoxHeight).toInt(),
                (m.textWidth * 2),
                (m.textWidth * 2), m.textWidth, m.textWidth)
        } else {
            g2d.drawRect((m.centreBoxWidth).toInt(),
                (m.centreBoxHeight).toInt(),
                (m.textWidth * 2),
                (m.textWidth * 2))
        }
        g2d.color = oldColour
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


    private fun paintLines(g2d: Graphics2D) {
        g2d.stroke = BasicStroke(getLineStroke(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        val bondsToPaint = uiData.getLineData()
        bondsToPaint.forEach { bondUI ->
            val startX = bondUI.startX * uiData.cameraZoom()
            val startY = bondUI.startY * uiData.cameraZoom()
            val endX = bondUI.endX * uiData.cameraZoom()
            val endY = bondUI.endY * uiData.cameraZoom()

            g2d.drawLine(startX.toInt(), startY.toInt(), endX.toInt(), endY.toInt())
        }
    }


    private fun paintTriangles(g2d: Graphics2D) {
        val triangles = uiData.getTriangleData()
        triangles.forEach { t ->
            val x = listOf<Int>((t.v1.x * uiData.cameraZoom()).toInt(), (t.v2.x * uiData.cameraZoom()).toInt(),
                (t.v3.x * uiData.cameraZoom()).toInt()
            ).toIntArray()
            val y = listOf<Int>((t.v1.y * uiData.cameraZoom()).toInt(), (t.v2.y * uiData.cameraZoom()).toInt(), (t.v3.y * uiData.cameraZoom()).toInt()).toIntArray()

            g2d.fillPolygon(x, y, 3)
        }
    }

    private fun paintBondSelection(g2d: Graphics2D) {
        val bondContexts = uiData.getBondData()
        val width = BOND_MARKER * uiData.cameraZoom()
        val height = BOND_MARKER * uiData.cameraZoom()
        val oldColour = g2d.color
        g2d.color = MolGLideUtils.getAccentColour()
        bondContexts.forEach { bondContext ->
            if (bondContext.isSelected) {
                val start = getDiscreteSelectionBoxStart(bondContext.midPoint, width.toFloat(), height.toFloat())
                g2d.fillRect(start.x.toInt(), start.y.toInt(), width.toInt(), height.toInt())
            }
        }
        g2d.color = oldColour
    }

    private fun getDiscreteSelectionBoxStart(bondPos: Vector2d, width: Float, height: Float): Vector2d {
        val startX = bondPos.x * uiData.cameraZoom()
        val startY = bondPos.y * uiData.cameraZoom()
        return Vector2d(startX - width / 2, startY - height / 2)
    }


    private fun paintGenericSelectionBox(g2d: Graphics2D) {
        var startX = uiData.cameraZoom() * uiData.getTransientSelectionStartX()
        var startY = uiData.cameraZoom() * uiData.getTransientSelectionStartY()
        var advX = uiData.cameraZoom() * uiData.getTransientSelectionAdvX()
        var advY = uiData.cameraZoom() * uiData.getTransientSelectionAdvY()
        //Java's painter ensures that width is positive, and height negative
        //So we need to convert that here if that is not the case
        if (advX < 0) {
            startX += advX
            advX = advX.absoluteValue
        }
        if (advY < 0) {
            startY += advY
            advY = advY.absoluteValue
        }

        val oldColour = g2d.color
        g2d.color = MolGLideUtils.getFocusColour()
        g2d.fillRect(startX.toInt(), startY.toInt(), advX.toInt(), advY.toInt())
        g2d.color = MolGLideUtils.getAccentColour()
        g2d.drawRect(startX.toInt(), startY.toInt(), advX.toInt(), advY.toInt())
        g2d.color = oldColour
    }

    private fun getFontSize() : Float {
        return UNMODDED_TEXT_SIZE * uiData.cameraZoom().toFloat()
    }

    private fun getLineStroke(): Float {
        return LINE_STROKE * uiData.cameraZoom().toFloat()
    }

    companion object {
        const val MOUSE_SENSE = 2.0
        const val MOUSE_SENSE_ZOOM = 0.5f
        const val UNMODDED_TEXT_SIZE = 32.0f
        const val SIG_MOUSE_DELTA = 2.0f
        const val LINE_STROKE = 3.0f
        const val BOND_MARKER = UNMODDED_TEXT_SIZE * 0.5f
    }
}