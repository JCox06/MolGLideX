package uk.co.jcox.molglide.editor.model

import org.joml.Vector2d
import javax.vecmath.Point2d
import kotlin.collections.map

class SelectionManager (
) {

    //todo this was a mistake doing this
    //Each object that can be placed in the editor, that can also be selected, should implement a new interface
    var primarySelection: Type = Type.None

    var batchSelection: BatchSelection = BatchSelection(mutableListOf(), mutableListOf())

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
        val atoms = mutableListOf<ChemAtom>()
        val bonds = mutableListOf<ChemBond>()

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

    fun clearSelectionBoundingBox() {
        batchSelection = BatchSelection(mutableListOf(), mutableListOf())
    }


    fun clearAndAddSelection(molecules: List<ChemMolecule>) {

        val selectedBonds = mutableListOf<ChemBond>()
        val selectedAtoms = mutableListOf<ChemAtom>()

        molecules.forEach { chemMolecule ->
            selectedAtoms.addAll(chemMolecule.atoms())
            selectedBonds.addAll(chemMolecule.bonds())
        }

        batchSelection = BatchSelection(selectedAtoms, selectedBonds)
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

    private fun getClosestAtom(levelData: EditorStateData, worldX: Int, worldY: Int) : Pair<ChemAtom, Double>?{
        val atoms = levelData.getAtoms()
        val x = worldX.toDouble()
        val y = worldY.toDouble()
        val lengthFromMouse = atoms.map {it to it.atom.point2d.distance(Point2d(x, y)) }
        val result = lengthFromMouse.minByOrNull {it.second}
        return result
    }

    private fun getClosestBond(levelData: EditorStateData, worldX: Int, worldY: Int): Pair<ChemBond, Double>? {
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
    fun getBond(): ChemBond? {
        val selection = primarySelection
        if (selection is Type.ActiveBond) {
            return selection.chemBond
        }
        return null
    }

    /**
     * This method is for discrete selections only
     * @return the currently selected atom or null if no bond is selected
     */
    fun getAtom(): ChemAtom? {
        val selection = primarySelection
        if (selection is Type.ActiveAtom) {
            return selection.chemAtom
        }
        return null
    }

    /**
     * This method is for discrete selections only
     * @return the currently selected molecule from either the currently selected atom or bond or null if not selected
     */
    fun getMolecule() : ChemMolecule? {
        val selection = primarySelection
        if (selection is Type.ActiveBond) {
            return selection.chemBond.molecule
        }
        if (selection is Type.ActiveAtom) {
            return selection.chemAtom.molecule
        }
        return null
    }

    /**
     * This method checks if the object is active either
     * in the primary selection (discrete) or if it is active
     * in the batch selection
     */
    fun isSelected(bond: ChemBond): Boolean {
        val p = primarySelection
        if (p is Type.ActiveBond && p.chemBond == bond) {
            return true
        }
        if (batchSelection.bonds.contains(bond)) {
            return true
        }
        return false
    }

    /**
     * This method checks if the object is active either
     * in the primary selection (discrete) or if it is active
     * in the batch selection
     */
    fun isSelected(atom: ChemAtom): Boolean {
        val p = primarySelection
        if (p is Type.ActiveAtom && p.chemAtom == atom) {
            return true
        }
        if (batchSelection.atoms.contains(atom)) {
            return true
        }
        return false
    }


    fun hasBatchSelection(): Boolean {
        return batchSelection.bonds.isNotEmpty() || batchSelection.atoms.isNotEmpty()
    }


    sealed class Type {


        /**
         * This checks to see if the mouse is hovered over an element
         * that was also selected in the AABB
         *
         * In other words if the user is hovering over something selected
         *
         * @return true if mouse is hovering on a selected object
         */
        abstract fun inPrimarySelection(batchSelection: BatchSelection): Boolean

        object None: Type() {
            override fun inPrimarySelection(batchSelection: BatchSelection): Boolean {
                return false
            }
        }

        data class ActiveAtom(val chemAtom: ChemAtom): Type() {
            override fun inPrimarySelection(batchSelection: BatchSelection): Boolean {
                return batchSelection.atoms.contains(chemAtom)
            }
        }

        data class ActiveBond(val chemBond: ChemBond): Type() {
            override fun inPrimarySelection(batchSelection: BatchSelection): Boolean {
                return batchSelection.bonds.contains(chemBond)
            }
        }
    }

    data class BatchSelection (
        val atoms: MutableList<ChemAtom>,
        val bonds: MutableList<ChemBond>
    )

    companion object {
        const val MIN_DIST: Int = 50
    }
}