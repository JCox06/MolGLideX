package uk.co.jcox.molglide.editor.model

import org.joml.Vector2d
import uk.co.jcox.molglide.editor.control.tool.Tool

class SelectionManager (
) {


    var primarySelection: SelectionInfo? = null

    var batchSelection = mutableSetOf<IEditorSelectable>()

    fun updatePrimarySelection(levelData: EditorStateData, worldX: Int, worldY: Int, currentTool: Tool) {
        val closestSelectable = getClosestSelectable(levelData, worldX, worldY)
        if (closestSelectable == null || !currentTool.isTypeValidPrimarySelection(closestSelectable.selectable)) {
            primarySelection = null
            return
        }
        if (closestSelectable.distanceFromMouse < MIN_DIST) {
            primarySelection = closestSelectable
            return
        }

        primarySelection = null
        return
    }


    fun updateSelectionBoundingBox(levelData: EditorStateData, x1: Int, y1: Int, x2: Int, y2: Int) {
        val items = mutableListOf<IEditorSelectable>()

        levelData.getSelectables().forEach { selectable ->
            val points = selectable.getObjectSelectionPoints()
            points.values.forEach { position ->
                if (checkInside(x1, y1, x2, y2, position)) {
                    items.add(selectable)
                }
            }

        }
        batchSelection.clear()
        batchSelection.addAll(items)
    }

    fun clearSelectionBoundingBox() {
        batchSelection.clear()
    }


    fun clearAndAddSelection(selectables: List<IEditorSelectable>) {
        batchSelection.clear()
        batchSelection.addAll(selectables)
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

    private fun getClosestSelectable(levelData: EditorStateData, worldX: Int, worldY: Int) : SelectionInfo? {
        val selectables = levelData.getSelectables()
        val x = worldX.toDouble()
        val y = worldY.toDouble()

        val fullSelection = mutableListOf<SelectionInfo>()
        selectables.forEach { selectable ->
            val points = selectable.getObjectSelectionPoints()
            points.forEach { (anchorID, anchor) ->
                val lengthFromMouse = anchor.distance(Vector2d(x, y))
                fullSelection.add(SelectionInfo(lengthFromMouse, anchorID, selectable))
            }
        }

        val result = fullSelection.minByOrNull { it.distanceFromMouse }
        return result
    }

    /**
     * This method is for discrete selections only
     * @return the currently selected bond or null if no bond is selected
     */
    fun getBond(): ChemBond? {
        val selection = primarySelection?.selectable
        if (selection is ChemBond) {
            return selection
        }
        return null
    }

    /**
     * This method is for discrete selections only
     * @return the currently selected atom or null if no bond is selected
     */
    fun getAtom(): ChemAtom? {
        val selectable = primarySelection?.selectable
        if (selectable is ChemAtom) {
            return selectable
        }
        return null
    }

    /**
     * This method is for discrete selections only
     * @return the currently selected molecule from either the currently selected atom or bond or null if not selected
     */
    fun getMolecule() : ChemMolecule? {
        val selection = primarySelection?.selectable
        if (selection is ChemAtom) {
            return selection.molecule
        }
        if (selection is ChemBond) {
            return selection.molecule
        }
        return null
    }



    /**
     * This method checks if the object is active either
     * in the primary selection (discrete) or if it is active
     * in the batch selection
     */
    fun isSelected(item: IEditorSelectable): Boolean {
        val p = primarySelection?.selectable
        if (p == item) {
            return true
        }
        if (batchSelection.contains(item)) {
            return true
        }
        return false
    }

    /**
     * Check to see if the following IEditorSelectable is selected by the
     * AABB
     */
    fun isAABBSelected(item: IEditorSelectable?): Boolean {
        if (item == null) return false
        if (batchSelection.contains(item)) {
            return true
        }
        return false
    }

    fun getBatchBonds(): List<ChemBond> {
        return batchSelection.filterIsInstance<ChemBond>()
    }

    fun getBatchAtoms(): List<ChemAtom> {
        return batchSelection.filterIsInstance<ChemAtom>()
    }

    fun getBatchSpatials(): Collection<ISpatialInfo> {
        return batchSelection.filterIsInstance<ISpatialInfo>()
    }


    fun hasBatchSelection(): Boolean {
        return batchSelection.isNotEmpty()
    }


    companion object {
        const val MIN_DIST: Int = 50
    }

    class SelectionInfo (
        val distanceFromMouse: Double,
        val objectAnchorID: Int,
        val selectable: IEditorSelectable,
    )
}