package uk.co.jcox.molglide.editor.model

import java.awt.Graphics2D

class UILine (
    val startX: Double,
    val startY: Double,
    val endX: Double,
    val endY: Double,
) : AbstractUIComponent(false) {

    override fun drawComponent(g2d: Graphics2D, cameraZoom: Double) {
        val startX = startX * cameraZoom
        val startY = startY * cameraZoom
        val endX = endX * cameraZoom
        val endY = endY * cameraZoom
        g2d.drawLine(startX.toInt(), startY.toInt(), endX.toInt(), endY.toInt())
    }
}