package uk.co.jcox.molglide.control

import org.joml.Vector2d
import javax.vecmath.Point2d

class SelectionManager (
) {

    var primarySelection: Type = Type.None

    var batchSelection: BatchSelection = BatchSelection(emptyList(), emptyList())

    fun updatePrimarySelection(levelData: EditorStateData, worldX: Int, worldY: Int) {
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


    fun updateSelectionBoundingBox(levelData: EditorStateData, x1: Int, y1: Int, x2: Int, y2: Int) {
        val atoms = mutableListOf<ChemMolecule.ChemAtom>()
        val bonds = mutableListOf<ChemMolecule.ChemBond>()

        levelData.getMolecules().forEach { chemMolecule ->
            chemMolecule.atoms().forEach { chemAtom ->
                val pos = chemAtom.getPos()
                if (checkInside(x1, y1, x2, y2, pos)) {
                    atoms.add(chemAtom)
                }
            }
            chemMolecule.bonds().forEach { chemBond ->
                val pos = chemBond.midPoint()
                if (checkInside(x1, y1, x2, y2, pos)) {
                    bonds.add(chemBond)
                }
            }
        }

        batchSelection = BatchSelection(atoms, bonds)
    }

    private fun checkInside(boxX1: Int, boxY1: Int, boxX2: Int, boxY2: Int, checkAgainst: Vector2d) : Boolean {
        val pointX = checkAgainst.x
        val pointY = checkAgainst.y

        if (pointX.toInt() in boxX1..boxX2 && pointY.toInt() in boxY1 .. boxY2) {
            return true
        }
        if (pointX.toInt() in boxX2..boxX1 && pointY.toInt() in boxY1 .. boxY2) {
            return true
        }
        if (pointX.toInt() in boxX2..boxX1 && pointY.toInt() in boxY2 .. boxY1) {
            return true
        }
        if (pointX.toInt() in boxX1..boxX2 && pointY.toInt() in boxY2 .. boxY1) {
            return true
        }
        return false
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


    /**
     * This method is for discrete selections only
     * @return the currently selected bond or null if no bond is selected
     */
    fun getBond(): ChemMolecule.ChemBond? {
        val selection = primarySelection
        if (selection is SelectionManager.Type.ActiveBond) {
            return selection.chemBond
        }
        return null
    }

    /**
     * This method is for discrete selections only
     * @return the currently selected atom or null if no bond is selected
     */
    fun getAtom(): ChemMolecule.ChemAtom? {
        val selection = primarySelection
        if (selection is SelectionManager.Type.ActiveAtom) {
            return selection.chemAtom
        }
        return null
    }

    /**
     * This method is for discrete selections only
     * @return the currently selected molecule from either the currently selected atom or bond or null if not selected
     */
    fun getMolecule() : ChemMolecule ? {
        val selection = primarySelection
        if (selection is SelectionManager.Type.ActiveBond) {
            return selection.chemBond.molecule
        }
        if (selection is SelectionManager.Type.ActiveAtom) {
            return selection.chemAtom.molecule
        }
        return null
    }

    sealed class Type {
        object None: Type()
        data class ActiveAtom(val chemAtom: ChemMolecule.ChemAtom): Type()
        data class ActiveBond(val chemBond: ChemMolecule.ChemBond): Type()
    }

    data class BatchSelection (
        val atoms: List<ChemMolecule.ChemAtom>,
        val bonds: List<ChemMolecule.ChemBond>
    )

    companion object {
        const val MIN_DIST: Int = 50
    }
}