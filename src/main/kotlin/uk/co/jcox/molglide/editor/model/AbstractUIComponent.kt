package uk.co.jcox.molglide.editor.model

import org.joml.Vector2d
import uk.co.jcox.molglide.MolGLideUtils
import uk.co.jcox.molglide.editor.ui.EditorPanel.Companion.BOND_MARKER
import java.awt.Graphics2D

abstract class AbstractUIComponent (var selected: Boolean) {
    abstract fun drawComponent(g2d: Graphics2D, cameraZoom: Double)

    open fun drawSelectionMarker(g2d: Graphics2D, cameraZoom: Double) {}

    protected fun getDiscreteSelectionBoxStart(camZoom: Double, startX: Float, startY: Float, width: Float, height: Float): Vector2d {
        val newStartX = startX * camZoom
        val newStartY = startY * camZoom
        return Vector2d(newStartX - width / 2, newStartY - height / 2)
    }

    protected fun drawSimpleSelection(g2d: Graphics2D, cameraZoom: Double, startX: Double, startY: Double) {
        val width = BOND_MARKER * cameraZoom
        val height = BOND_MARKER * cameraZoom
        val oldColour = g2d.color
        g2d.color = MolGLideUtils.getAccentColour()
        val newStart = getDiscreteSelectionBoxStart(cameraZoom, startX.toFloat(), startY.toFloat(), width.toFloat(), height.toFloat())
        g2d.fillRect(newStart.x.toInt(), newStart.y.toInt(), width.toInt(), height.toInt())
        g2d.color = oldColour
    }
}