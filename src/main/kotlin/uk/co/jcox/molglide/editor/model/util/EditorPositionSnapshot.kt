package uk.co.jcox.molglide.editor.model.util

import org.checkerframework.checker.units.qual.s
import org.joml.Vector2d
import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.ISpatialInfo

class EditorPositionSnapshot (
    atomList: Collection<ISpatialInfo>
) {

    private val posMap: MutableMap<ISpatialInfo, Map<Int, Vector2d>> = mutableMapOf()

    init {
        takeSnapshot(atomList)
    }


    fun takeSnapshot(spatialInfoList: Collection<ISpatialInfo>) {
        spatialInfoList.forEach { spatialInfo ->
            posMap[spatialInfo] = spatialInfo.getAllCoordinates()
        }
    }

    operator fun get(spatial: ISpatialInfo): Map<Int, Vector2d> {
        return posMap[spatial] ?: throw NullPointerException("Cannot provide a position which was not supplied")
    }

    fun applySnapshot(spatial: ISpatialInfo) {
        val originalPos = posMap[spatial] ?: return
        spatial.pushNewCoordinates(originalPos)
    }

    fun applyAll() {
        posMap.keys.forEach { key ->
            applySnapshot(key)
        }
    }

    /**
     * Translates all ISpatialInfo components with a dx, and dy parameter
     * from the original positions the component was in based on when this object
     * was created
     */
    fun translateCoordinates(dx: Double, dy: Double) {
        posMap.keys.forEach { spatial ->
            val originalPositions = posMap[spatial] ?: return@forEach
            val newPositions = originalPositions.mapValues {
                Vector2d(it.value.x + dx, it.value.y + dy)
            }
            spatial.pushNewCoordinates(newPositions)
        }
    }

    fun rotateCoordinates(centreX: Double, centreY: Double, angle: Double) {
        posMap.keys.forEach { spatial ->
            val originalPositions = posMap[spatial] ?: return@forEach
            val newPositions = originalPositions.mapValues {
                val newVec = it.value.rotateAround(angle, centreX, centreY, Vector2d())
                return@mapValues newVec
            }
            spatial.pushNewCoordinates(newPositions)
        }
    }


    /**
     * Returns a new position snapshot of the current positions
     * of all the ISpatiaInfo components
     */
    fun newSnapshot(): EditorPositionSnapshot {
        return EditorPositionSnapshot(posMap.keys)
    }


    companion object {
        fun ofMolecule(chemMolecule: ChemMolecule): EditorPositionSnapshot {
            return EditorPositionSnapshot(chemMolecule.atoms())
        }
    }
}