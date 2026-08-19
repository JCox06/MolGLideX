package uk.co.jcox.molglide.control

import org.joml.Vector2d
import java.awt.Graphics2D

abstract class AbstractUIComponent (var selected: Boolean) {
    abstract fun drawComponent(g2d: Graphics2D, cameraZoom: Double)

    open fun drawSelectionMarker(g2d: Graphics2D, cameraZoom: Double) {}
}