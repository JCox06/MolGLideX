package uk.co.jcox.molglide.editor.model

import org.apache.jena.riot.other.G
import org.checkerframework.checker.units.qual.m
import org.joml.Vector2d
import org.openscience.cdk.smiles.smarts.parser.SMARTSParserConstants.x
import uk.co.jcox.molglide.MolGLideUtils
import uk.co.jcox.molglide.editor.ui.MasterAtomMetric
import java.awt.Color
import java.awt.Graphics2D
import java.awt.font.TextAttribute
import java.awt.font.TextLayout
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

    var x = 0.0
    var y = 0.0

    var textWidth = 0
    var textHeight = 0
    var centreTextWidth = 0.0
    var centreTextHeight = 0.0
    var centreBoxWidth = 0.0
    var centreBoxHeight = 0.0

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

    private fun setupMetrics(g2d: Graphics2D, cameraZoom: Double) {
        x = posX * cameraZoom
        y = posY * cameraZoom
        textWidth = g2d.fontMetrics.stringWidth(element)
        textHeight = g2d.fontMetrics.ascent - g2d.fontMetrics.descent
        centreTextWidth = x - textWidth / 2
        centreTextHeight = y + textHeight / 2
        centreBoxWidth = x - textWidth
        centreBoxHeight = y - g2d.fontMetrics.height / 2
    }


    //Is used for the atom selection marker, but also for any errors that may arise
    private fun paintAtomTextBoxBorder(g2d: Graphics2D, color: Color, shouldFill: Boolean) {
        val oldColour = g2d.color
        val newColour = color
        g2d.color = newColour
        if (shouldFill) {
            g2d.fillRoundRect((centreBoxWidth).toInt(),
                (centreBoxHeight).toInt(),
                (textWidth * 2),
                (textWidth * 2), textWidth, textWidth)
        } else {
            g2d.drawRect((centreBoxWidth).toInt(),
                (centreBoxHeight).toInt(),
                (textWidth * 2),
                (textWidth * 2))
        }
        g2d.color = oldColour
    }

    private fun paintMainAtomElementSymbol(g2d: Graphics2D) {
        g2d.drawString(element, centreTextWidth.toInt(), centreTextHeight.toInt())
    }

    private fun paintTrailGroup(g2d: Graphics2D) {
        val attributedString = AttributedString(trailGroup)
        attributedString.addAttribute(TextAttribute.FAMILY, g2d.font.family)
        attributedString.addAttribute(TextAttribute.SIZE, g2d.font.size)

        val subscriptRange = getSubscriptRange(trailGroup)
        subscriptRange.forEach { attributedString.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUB, it, it+1) }

        val renderingContext = g2d.fontRenderContext
        val attributorIterator = attributedString.iterator

        val textLayout = TextLayout(attributorIterator, renderingContext)
        val position = getTextDrawForTrailPos(g2d, textLayout)

        textLayout.draw(g2d, position.x.toFloat(), position.y.toFloat())
    }

    private fun getTextDrawForTrailPos(g2d: Graphics2D, textLayout: TextLayout) : Vector2d {
        val defaultStartX = x + textWidth / 2
        val defaultStartY = y + textHeight / 2

        if (trailGroupPos == ChemMolecule.TrailingGroupPosition.RIGHT) {
            return Vector2d(defaultStartX, defaultStartY)
        }
        if (trailGroupPos == ChemMolecule.TrailingGroupPosition.LEFT) {
            val newX = defaultStartX - textLayout.bounds.width - textWidth
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
}