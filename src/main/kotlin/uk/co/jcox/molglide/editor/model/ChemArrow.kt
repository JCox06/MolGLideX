package uk.co.jcox.molglide.editor.model

import org.joml.Vector2d
import kotlin.collections.set

class ChemArrow (
    val arrowPoints: MutableMap<Int, Vector2d>,
    var startArrow: ArrowHead = ArrowHead.NONE,
    var endArrow: ArrowHead = ArrowHead.DOUBLE_BARBED
) : IEditorSelectable, IChemComponent {

    private var transient = false


    fun start() : Vector2d {
        val start = arrowPoints[0] ?: Vector2d()
        return start
    }

    fun end() : Vector2d {
        val end = arrowPoints[1] ?: Vector2d()
        return end
    }

    fun controlA() : Vector2d {
        val controlA = arrowPoints[2] ?: Vector2d()
        return controlA
    }

    override fun getObjectSelectionPoints(): Map<Int, Vector2d> {
        return arrowPoints
    }

    override fun isTransient(): Boolean {
        return transient
    }

    override fun setTransient(value: Boolean) {
        transient = value
    }


    enum class ArrowHead {
        DOUBLE_BARBED,
        SINGLE_BARBED,
        NONE,
    }

    companion object {
        fun ofSimple(start: Vector2d, end: Vector2d, control: Vector2d, startArrow: ArrowHead = ArrowHead.NONE, endArrow: ArrowHead = ArrowHead.DOUBLE_BARBED) : ChemArrow {
            val points = mutableMapOf(
                0 to start,
                1 to end,
                2 to control,
            )
            return ChemArrow(points, startArrow, endArrow)
        }
    }
}