package uk.co.jcox.molglide.control

import org.joml.Vector2d
import javax.vecmath.Point2d

class SelectionManager (
) {

    var primarySelection: Type = Type.None

    fun update(levelData: EditorStateData, worldX: Int, worldY: Int) {
        val closestAtom = getClosestAtom(levelData, worldX, worldY)
        val closestBond = getClosestBond(levelData, worldX, worldY)

        if (closestAtom == null && closestBond != null && closestBond.second < MIN_DIST) {
            primarySelection = Type.ActiveBond(closestBond.first)
            return
        }
        if (closestBond == null && closestAtom != null && closestAtom.second < MIN_DIST) {
            primarySelection = Type.ActiveAtom(closestAtom.first)
            return
        }
        if (closestAtom != null && closestBond != null) {
            if (closestAtom.second < closestBond.second && closestAtom.second < MIN_DIST) {
                primarySelection = Type.ActiveAtom(closestAtom.first)
                return
            }
            if (closestBond.second < MIN_DIST) {
                primarySelection = Type.ActiveBond(closestBond.first)
                return
            }
        }
        primarySelection = Type.None
    }

    private fun getClosestAtom(levelData: EditorStateData, worldX: Int, worldY: Int) : Pair<ChemMolecule.ChemAtom, Double>?{
        val atoms = levelData.getAtoms()
        val x = worldX.toDouble()
        val y = worldY.toDouble()
        val lengthFromMouse = atoms.map {it to it.atom.point2d.distance(Point2d(x, y)) }
        val result = lengthFromMouse.minByOrNull {it.second}
        return result
    }

    private fun getClosestBond(levelData: EditorStateData, worldX: Int, worldY: Int): Pair<ChemMolecule.ChemBond, Double>? {
        val bonds = levelData.getBonds()
        val x = worldX.toDouble()
        val y = worldY.toDouble()
        val lengthFromMouse = bonds.map { it to it.midPoint().distance(Vector2d(x, y)) }
        val result = lengthFromMouse.minByOrNull {it.second}
        return result
    }


    sealed class Type {
        object None: Type()
        data class ActiveAtom(val chemAtom: ChemMolecule.ChemAtom): Type()
        data class ActiveBond(val chemBond: ChemMolecule.ChemBond): Type()
    }

    companion object {
        const val MIN_DIST: Int = 50
    }
}