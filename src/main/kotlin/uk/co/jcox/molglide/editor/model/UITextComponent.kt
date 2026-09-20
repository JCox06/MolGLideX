package uk.co.jcox.molglide.editor.model

import uk.co.jcox.molglide.MolGLideUtils
import java.awt.Color
import java.awt.Graphics2D
import java.awt.font.TextAttribute
import java.text.AttributedString

open class UITextComponent (
    val element: String,
    val posX: Double,
    val posY: Double,
    selected: Boolean
) : AbstractUIComponent(selected) {

    var x = 0.0
    var y = 0.0

    var textWidth = 0
    var textHeight = 0
    var centreTextWidth = 0.0
    var centreTextHeight = 0.0
    var centreBoxWidth = 0.0
    var centreBoxHeight = 0.0


    protected fun setupMetrics(g2d: Graphics2D, cameraZoom: Double) {
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
    protected fun paintAtomTextBoxBorder(g2d: Graphics2D, color: Color, shouldFill: Boolean) {
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



    private fun paintText(g2d: Graphics2D) {
        //todo fix selection marker for this!
        val attStr = AttributedString(element)
        attStr.addAttribute(TextAttribute.FAMILY, g2d.font.family)
        attStr.addAttribute(TextAttribute.SIZE, g2d.font.size)
        attStr.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUPER)
        g2d.drawString(attStr.iterator, x.toInt(), y.toInt())
    }

    override fun drawComponent(g2d: Graphics2D, cameraZoom: Double) {
        setupMetrics(g2d, cameraZoom)
        paintText(g2d)
    }

    override fun drawSelectionMarker(g2d: Graphics2D, cameraZoom: Double) {
        if (selected) {
            setupMetrics(g2d, cameraZoom)
            paintAtomTextBoxBorder(g2d, MolGLideUtils.getAccentColour(), true)
        }
    }
}