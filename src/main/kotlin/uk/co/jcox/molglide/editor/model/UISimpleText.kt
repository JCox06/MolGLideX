package uk.co.jcox.molglide.editor.model

import java.awt.Graphics2D

class UISimpleText (
    private val posX: Double,
    private val posY: Double,
    private val text: String,
    selected: Boolean
) : AbstractUIComponent(selected) {

    override fun drawComponent(g2d: Graphics2D, cameraZoom: Double) {
        val x = posX * cameraZoom
        val y = posY * cameraZoom
        g2d.drawString("C", x.toInt(), y.toInt())
    }
}