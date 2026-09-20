package uk.co.jcox.molglide.editor.model

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

        if (trailGroup == "") {
            g2d.drawString(element, centreTextWidth.toInt(), centreTextHeight.toInt())

            if (trailGroupPos == ChemMolecule.TrailingGroupPosition.ABOVE || trailGroupPos == ChemMolecule.TrailingGroupPosition.BELOW) {
                drawSeparateAboveBelowTrailingPos(g2d)
            }
            return
        }

        if (trailGroupPos == ChemMolecule.TrailingGroupPosition.LEFT) {
            drawIntegratedLeftTrailingPos(g2d)
            return
        }

        if (trailGroupPos == ChemMolecule.TrailingGroupPosition.RIGHT) {
            drawIntegratedRightTrailingPos(g2d)
            return
        }
    }


    private fun drawSeparateAboveBelowTrailingPos(g2d: Graphics2D) {
        TODO()
    }

    //todo Need a method like:
    //g2d#drawStringWithOrigin - Where the origin is an index to the char of the string to draw
    //where the x, and y positions are applied.
    //In this case, the main atom would be the origin!
    private fun drawIntegratedLeftTrailingPos(g2d: Graphics2D) {
        val fullChemText = "${trailGroup}${element}"
        val fullChemFormula = getChemFormulaString(g2d, fullChemText)
        val trailChemFormula = getChemFormulaString(g2d, trailGroup)
        val trailWidth = trailChemFormula.bounds.width
        val newX = centreTextWidth.toFloat() - trailWidth.toFloat()
        fullChemFormula.draw(g2d, newX, centreTextHeight.toFloat())
    }

    private fun drawIntegratedRightTrailingPos(g2d: Graphics2D) {
        val fullChemText = "${element}${trailGroup}"
        val chemFormula = getChemFormulaString(g2d, fullChemText)
        chemFormula.draw(g2d, centreTextWidth.toFloat(), centreTextHeight.toFloat())
    }

    private fun getChemFormulaString(g2d: Graphics2D, chemText: String): TextLayout {
        val attStr = AttributedString(chemText)
        attStr.addAttribute(TextAttribute.FAMILY, g2d.font.family)
        attStr.addAttribute(TextAttribute.SIZE, g2d.font.size)

        val subscriptRange = getSubscriptRange(chemText)
        subscriptRange.forEach { attStr.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUB, it, it+1) }

        val renderingCtx = g2d.fontRenderContext
        val attIterator = attStr.iterator
        return TextLayout(attIterator, renderingCtx)
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