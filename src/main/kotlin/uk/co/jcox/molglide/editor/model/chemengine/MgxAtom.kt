package uk.co.jcox.molglide.editor.model.chemengine

import com.sun.org.apache.xpath.internal.operations.Bool
import org.joml.Vector2d
import org.joml.Vector2dc

interface MgxAtom : ISpatialInfo, IEditorSelectable {

    val isCarbon: Boolean

    fun getSymbol(): String

    fun isNotImplicit(): Boolean

    fun setNotImplicit(implicit: Boolean)

    fun ignoreErrors(): Boolean

    fun setIgnoreErrors(ignore: Boolean)

    fun getTrailPos(): TrailingGroupPosition

    fun setTrailPos(pos: TrailingGroupPosition)

    fun getPos(): Vector2d

    fun setPos(xPos: Double, yPos: Double)

    fun getFormalCharge(): Int

    fun setFormalCharge(fc: Int)

    fun getMolecule(): MgxMolecule

    fun getAtomTypeString(): String

    fun isAtomTypeResolved(): Boolean

    fun getImplicitHCount(): Int

    fun createFormalChargeAt(xPos: Double, yPos: Double): MgxFormalCharge

    override fun getAllCoordinates(): Map<Int, Vector2d> {
        return mapOf(MAIN_ATOM to getPos())
    }

    override fun pushNewCoordinates(coordinateMap: Map<Int, Vector2d>) {
        val xyz = coordinateMap[MAIN_ATOM]
        if (xyz != null) {
            setPos(xyz.x, xyz.y)
        }
    }

    override fun getObjectSelectionPoints(): Map<Int, Vector2dc> {
        return mapOf(MAIN_ATOM to getPos())
    }

    enum class TrailingGroupPosition (val vec: Vector2d) {
        ABOVE(Vector2d(0.0, 1.0)),
        BELOW(Vector2d(0.0, -1.0)),
        LEFT(Vector2d(-1.0, 0.0)),
        RIGHT(Vector2d(1.0, 0.0)),
    }

    companion object {
        const val MAIN_ATOM = 0
    }
}