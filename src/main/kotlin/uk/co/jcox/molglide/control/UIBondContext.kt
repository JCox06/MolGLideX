package uk.co.jcox.molglide.control

import org.joml.Vector2d

data class UIBondContext (
    val order: Int,
    val midPoint: Vector2d,
    val isAromatic: Boolean,
    val isCentre: Boolean?,
)