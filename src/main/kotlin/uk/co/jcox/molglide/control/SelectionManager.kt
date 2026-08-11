package uk.co.jcox.molglide.control

import javax.vecmath.Point2d

class SelectionManager (
) {

    var primary: Type = Type.None

    fun update(levelData: EditorStateData, worldX: Int, worldY: Int) {
        val closestAtom = getClosestAtom(levelData, worldX, worldY)
        if (closestAtom != null && closestAtom.second < MIN_DIST) {
            primary = Type.Active(closestAtom.first)
            return
        }
        primary = Type.None
    }

    private fun getClosestAtom(levelData: EditorStateData, worldX: Int, worldY: Int) : Pair<ChemMolecule.ChemAtom, Double>?{
        val atoms = levelData.getAtoms()
        val x = worldX.toDouble()
        val y = worldY.toDouble()
        val lengthFromMouse = atoms.map {it to it.atom.point2d.distance(Point2d(x, y)) }
        val result = lengthFromMouse.minByOrNull {it.second}
        return result
    }



    sealed class Type {
        object None: Type()
        data class Active(val chemAtom: ChemMolecule.ChemAtom): Type()
    }

    companion object {
        const val MIN_DIST: Int = 50
    }
}