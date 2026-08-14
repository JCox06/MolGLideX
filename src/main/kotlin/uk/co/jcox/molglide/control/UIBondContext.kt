package uk.co.jcox.molglide.control

import org.joml.Vector2d
import uk.co.jcox.molglide.StereoChem

data class UIBondContext (
    val order: Int,
    val midPoint: Vector2d,
    val isAromatic: Boolean,
    val stereo: StereoChem
    )