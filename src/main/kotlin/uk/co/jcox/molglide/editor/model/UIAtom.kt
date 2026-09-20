package uk.co.jcox.molglide.editor.model

import org.joml.Vector2d
import uk.co.jcox.molglide.MolGLideUtils
import java.awt.Color
import java.awt.Graphics2D
import java.awt.font.TextAttribute
import java.awt.font.TextLayout
import java.text.AttributedString

class UIAtom (
    element: String,
    posX: Double,
    posY: Double,
    val trailGroup: String,
    val trailGroupPos: ChemMolecule.TrailingGroupPosition,
    val visible: Boolean,
    selected: Boolean,
    val hasErrors: Boolean,
    val ignoreErrors: Boolean,
) : UITextComponent (element, posX, posY, selected) {

    override fun drawComponent(g2d: Graphics2D, cameraZoom: Double) {
        if (! visible) {
            return
        }
        setupMetrics(g2d, cameraZoom)
        paintMainAtomElementSymbol(g2d)

        if (trailGroup != "") {
            paintTrailGroup(g2d)
        }
    }

    override fun drawSelectionMarker(g2d: Graphics2D, cameraZoom: Double) {
        if (selected || (hasErrors && !ignoreErrors)) {
            setupMetrics(g2d, cameraZoom)
            if (selected) {
                paintAtomTextBoxBorder(g2d, MolGLideUtils.getAccentColour(), true)
            }
            if (hasErrors && !ignoreErrors) {
                paintAtomTextBoxBorder(g2d, Color.RED, false)
            }
        }
    }

    private fun paintMainAtomElementSymbol(g2d: Graphics2D) {
        g2d.drawString(element, centreTextWidth.toInt(), centreTextHeight.toInt())
    }

    private fun paintTrailGroup(g2d: Graphics2D) {

        val trailGroupTextLayout = getChemTextLayout(g2d, trailGroup)
        val leftLeaningTextLayout = getChemTextLayout(g2d, "${trailGroup}${element}")

        val position = getTextDrawForTrailPos(g2d, leftLeaningTextLayout)

        trailGroupTextLayout.draw(g2d, position.x.toFloat(), position.y.toFloat())
    }

    //todo come back to this for the custom label system!
    private fun getChemTextLayout(g2d: Graphics2D, chemText: String): TextLayout {
        val attStr = AttributedString(chemText)
        attStr.addAttribute(TextAttribute.FAMILY, g2d.font.family)
        attStr.addAttribute(TextAttribute.SIZE, g2d.font.size)

        val subscriptRange = getSubscriptRange(chemText)
        subscriptRange.forEach { attStr.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUB, it, it+1) }

        val renderingContext = g2d.fontRenderContext
        val attributorIterator = attStr.iterator

        val textLayout = TextLayout(attributorIterator, renderingContext)
        return textLayout
    }

    private fun getTextDrawForTrailPos(g2d: Graphics2D, leftLeaningTextLayout: TextLayout) : Vector2d {
        val defaultStartX = x + textWidth.toDouble() / 2
        val defaultStartY = y + textHeight.toDouble() / 2

        if (trailGroupPos == ChemMolecule.TrailingGroupPosition.RIGHT) {
            return Vector2d(defaultStartX, defaultStartY)
        }
        if (trailGroupPos == ChemMolecule.TrailingGroupPosition.LEFT) {
            val newX = defaultStartX - leftLeaningTextLayout.bounds.width
            val newY = defaultStartY
            return Vector2d(newX, newY)
        }
        return Vector2d()
    }

    private fun getSubscriptRange(trailGroup: String): List<Int> {
        val list = ArrayList<Int>()
        trailGroup.forEachIndexed { index, ch ->
            if (ch.isDigit()) {
                list.add(index)
            }
        }
        return list
    }

    private fun getSuperscriptRange(label: String): List<Int> {
        val list = ArrayList<Int>()
        label.forEachIndexed { index, ch ->
            if (ch == '+' || ch == '-' || ch.isDigit()) {
                list.add(index)
            }
        }
        return list
    }
}