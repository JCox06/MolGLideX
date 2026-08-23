package uk.co.jcox.molglide.editor.model

import org.joml.Vector2d
import org.joml.minus
import org.joml.times
import java.awt.Graphics2D
import java.awt.geom.QuadCurve2D

class UIChemArrow(
    private val start: Vector2d,
    private val end: Vector2d,
    private val mid: Vector2d,
    private val components: List<AbstractUIComponent>,
    selected: Boolean
) : AbstractUIComponent(selected) {

    override fun drawComponent(g2d: Graphics2D, cameraZoom: Double) {
        val newStart = start * cameraZoom
        val newEnd = end * cameraZoom
        val newMid = mid * cameraZoom

        val curve = QuadCurve2D.Double(
            newStart.x, newStart.y,
            newMid.x, newMid.y,
            newEnd.x, newEnd.y
        )
        g2d.draw(curve)

        components.forEach { it.drawComponent(g2d, cameraZoom) }
    }


    override fun drawSelectionMarker(g2d: Graphics2D, cameraZoom: Double) {
        if (selected) {
            drawSimpleSelection(g2d, cameraZoom, start.x, start.y)
            drawSimpleSelection(g2d, cameraZoom, end.x, end.y)
            drawSimpleSelection(g2d, cameraZoom, mid.x, mid.y)
        }
    }
}