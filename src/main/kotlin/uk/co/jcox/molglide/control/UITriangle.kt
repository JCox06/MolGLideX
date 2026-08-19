package uk.co.jcox.molglide.control

import org.joml.Vector2d
import java.awt.Graphics2D

data class UITriangle (
    val v1: Vector2d,
    val v2: Vector2d,
    val v3: Vector2d
) : AbstractUIComponent(false) {

    override fun drawComponent(g2d: Graphics2D, cameraZoom: Double) {
        val x = listOf<Int>((v1.x * cameraZoom).toInt(), (v2.x * cameraZoom).toInt(),
            (v3.x * cameraZoom).toInt()
        ).toIntArray()
        val y = listOf<Int>((v1.y * cameraZoom).toInt(), (v2.y * cameraZoom).toInt(), (v3.y * cameraZoom).toInt()).toIntArray()

        g2d.fillPolygon(x, y, 3)
    }
}