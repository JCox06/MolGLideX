package uk.co.jcox.molglide.editor.model.chemengine

import org.joml.Vector2d
import org.joml.Vector2dc

interface MgxFormalCharge : IEditorSelectable, ISpatialInfo {


    fun getCharge(): Int

    fun setCharge(charge: Int)

    fun getPos(): Vector2d

    fun setPos(xPos: Double, yPos: Double)

    fun getAssociatedAtom(): MgxAtom

    override fun getAllCoordinates(): Map<Int, Vector2d> {
        return mapOf(0 to getPos())
    }

    override fun pushNewCoordinates(coordinateMap: Map<Int, Vector2d>) {
        val newPos = coordinateMap[MAIN_CHARGE] ?: return
        setPos(newPos.x, newPos.y)
    }

    override fun getObjectSelectionPoints(): Map<Int, Vector2dc> {
        return mapOf(0 to getPos())
    }

    companion object {
        private const val MAIN_CHARGE = 0
    }
}