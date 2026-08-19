package uk.co.jcox.molglide.editor.ui

import uk.co.jcox.molglide.MolGLideUtils
import uk.co.jcox.molglide.editor.model.IDataModelUI
import java.awt.BasicStroke
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JPanel
import kotlin.math.absoluteValue


class EditorPanel(private val uiData: IDataModelUI) : JPanel() {

    /**
     * Called by the controller 60 times a second
     */
    fun refreshEditor() {
        repaint()
    }

    fun paintEditor(g2d: Graphics2D, drawTransients: Boolean = true, onlyDrawSelected: Boolean = false) {
        preparePainter(g2d)
        paintGenericSelectionBox(g2d)
        g2d.stroke = BasicStroke(getLineStroke(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)

        uiData.getUIComponents().forEach { uiComp ->
            if (drawTransients) {
                uiComp.drawSelectionMarker(g2d, uiData.cameraZoom())
            }
            if (!uiComp.selected && onlyDrawSelected) {
                return@forEach
            }
            uiComp.drawComponent(g2d, uiData.cameraZoom())
        }
    }

    protected override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        val g2d = g as? Graphics2D ?: return
        paintEditor(g2d, true, false)
    }

    private fun preparePainter(g2d: Graphics2D) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g2d.font = Font("Liberation Serif", Font.PLAIN, -5)
        g2d.font = g2d.font.deriveFont(Font.PLAIN, getFontSize())

        g2d.translate(uiData.cameraX(), uiData.cameraY())
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