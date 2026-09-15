package uk.co.jcox.molglide.editor.model

import org.joml.Vector2d

class ChemFormalCharge (
    val position: Vector2d,
    val chemAtom: ChemAtom,
) : IEditorSelectable, ISpatialInfo, IChemComponent{

    private var isTransient = false

    override fun getObjectSelectionPoints(): Map<Int, Vector2d> {
        return mapOf(MAIN to position)
    }

    override fun getAllCoordinates(): Map<Int, Vector2d> {
        return mapOf(MAIN to Vector2d(position))
    }

    override fun pushNewCoordinates(coordinateMap: Map<Int, Vector2d>) {
        val newCoordinates = coordinateMap[MAIN]
        if (newCoordinates != null) {
            position.x = newCoordinates.x
            position.y = newCoordinates.y
        }
    }

    override fun isTransient(): Boolean {
        return isTransient || chemAtom.isTransient()
    }

    override fun setTransient(value: Boolean) {
        isTransient = value
    }

    fun getCharge(): Int {
        return chemAtom.getFormalCharge()
    }

    fun setCharge(value: Int) {
        chemAtom.setFormalCharge(value)
    }

    companion object {
        const val MAIN = 0
    }
}