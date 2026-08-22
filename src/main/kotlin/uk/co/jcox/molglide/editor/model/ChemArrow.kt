package uk.co.jcox.molglide.editor.model

import org.joml.Vector2d

class ChemArrow (
    var start: Vector2d,
    var end: Vector2d,
    var midAnchor: Vector2d? = null,
    var startArrow: ArrowHead = ArrowHead.NONE,
    var endArrow: ArrowHead = ArrowHead.DOUBLE_BARBED
) : IEditorSelectable {

    override fun getSelectionPosition(): Vector2d {
        val a = midAnchor ?: return start.lerp(end, 0.5, Vector2d())
        return a
    }


    enum class ArrowHead {
        DOUBLE_BARBED,
        SINGLE_BARBED,
        NONE,
    }
}