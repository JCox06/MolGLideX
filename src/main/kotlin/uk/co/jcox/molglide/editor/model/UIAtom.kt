package uk.co.jcox.molglide.editor.model

import org.joml.Vector2d
import uk.co.jcox.molglide.MolGLideUtils
import uk.co.jcox.molglide.editor.ui.MasterAtomMetric
import java.awt.Color
import java.awt.Graphics2D
import java.awt.font.TextAttribute
import java.text.AttributedString

class UIAtom (
    val element: String,
    val posX: Double,
    val posY: Double,
    val trailGroup: String,
    val trailGroupPos: ChemMolecule.TrailingGroupPosition,
    val visible: Boolean,
    selected: Boolean,
    val hasErrors: Boolean,
    val ignoreErrors: Boolean,
) : AbstractUIComponent(selected) {

    private fun calculateMasterMetrics(g2d: Graphics2D, x: Double, y: Double): MasterAtomMetric {
        val textWidth = g2d.fontMetrics.stringWidth(element)
        val textHeight = g2d.fontMetrics.ascent - g2d.fontMetrics.descent

        val centreTextWidth = x - textWidth / 2
        val centreBoxWidth = x - textWidth
        val centreTextHeight = y + textHeight / 2
        val centreBoxHeight = y - g2d.fontMetrics.height / 2
        return MasterAtomMetric(centreTextWidth, centreTextHeight, centreBoxWidth, centreBoxHeight, textWidth, textHeight)
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

    //Is used for the atom selection marker, but also for any errors that may arise
    private fun paintAtomTextBoxBorder(g2d: Graphics2D, m: MasterAtomMetric, color: Color, shouldFill: Boolean) {
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

    private fun paintMasterAtom(g2d: Graphics2D, m: MasterAtomMetric) {
        g2d.drawString(element, m.centreTextWidth.toInt(), m.centreTextHeight.toInt())
    }

    private fun paintTrailGroup(g2d: Graphics2D, x: Double, y: Double, m: MasterAtomMetric) {

        val s = getStartingPos(g2d,x, y, m)
        val startX = s.x
        val startY = s.y

        val attString = AttributedString(trailGroup)
        attString.addAttribute(TextAttribute.FAMILY, g2d.font.family)
        attString.addAttribute(TextAttribute.SIZE, g2d.font.size)

        val range = getSubscriptRange(trailGroup)
        range.forEach {
            attString.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUB,  it, it + 1)
        }
        g2d.drawString(attString.iterator, startX.toFloat(), startY.toFloat())
    }

    private fun getStartingPos(g2d: Graphics2D, x: Double, y: Double, m: MasterAtomMetric) : Vector2d {
        val startX = x + m.textWidth / 2
        val startY = y + m.textHeight / 2

        if (trailGroupPos == ChemMolecule.TrailingGroupPosition.RIGHT) {
            return Vector2d(startX, startY)
        }
        if (trailGroupPos == ChemMolecule.TrailingGroupPosition.LEFT) {
            val trail = trailGroup
            val textWidth = g2d.fontMetrics.stringWidth(trail)
            return Vector2d(startX - textWidth - m.textWidth, startY)
        }


        return Vector2d(startX, startY)
    }


    override fun drawComponent(g2d: Graphics2D, cameraZoom: Double) {
        val x = posX * cameraZoom
        val y = posY * cameraZoom
        val metrics = calculateMasterMetrics(g2d, x, y)

        if (visible) {
            paintMasterAtom(g2d,metrics)
            if (trailGroup != "") {
                paintTrailGroup(g2d, x, y, metrics)
            }
        }
    }

    override fun drawSelectionMarker(g2d: Graphics2D, cameraZoom: Double) {
        //Draw the selection marker
        val x = posX * cameraZoom
        val y = posY * cameraZoom
        val metrics = calculateMasterMetrics(g2d, x, y)
        if (selected) paintAtomTextBoxBorder(g2d, metrics, MolGLideUtils.getAccentColour(), true)

        //Check if error
        val red = Color.red
        if (!ignoreErrors && hasErrors) paintAtomTextBoxBorder(g2d, metrics, red, false)
    }
}