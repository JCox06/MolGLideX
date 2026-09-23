package uk.co.jcox.molglide.editor.model.chemengine

import org.joml.Vector2d
import org.joml.Vector2dc

/**
 * Represents an atom in MolGLide that can be placed within the editor
 * It contains chemical and spatial data, as well as instructions on how the renderer (UIDataBuilder) should
 * build the UI
 *
 * The Mgx selection of classes are implementation independent of the underlying chemical structure model
 *
 * Get an instance of this class from the associated MgxMolecule
 */
interface MgxAtom : ISpatialInfo, IEditorSelectable {

    val isCarbon: Boolean

    /**
     * @return the chemical symbol (e.g. C, Fe, ...)
     */
    fun getSymbol(): String

    /**
     * @return true if the chemical symbol should be rendered in the editor
     * For instance, carbons in aliphatic chains are generally not visible, and so implicit
     */
    fun isNotImplicit(): Boolean

    fun setNotImplicit(implicit: Boolean)

    /**
     * The UIDataBuilder may sometimes flag atoms as having errors
     * Atoms can be flagged if they have an incorrect valency
     * @return true to indicate to the UIDataBuilder that this Atom is ignoring errors
     */
    fun ignoreErrors(): Boolean

    fun setIgnoreErrors(ignore: Boolean)

    /**
     * Implicit hydrogens, for example in CH4, or NH3, need to be drawn somewhere
     * They can be drawn from all four sides of the atom.
     * @return the trail position
     */
    fun getTrailPos(): TrailingGroupPosition

    fun setTrailPos(pos: TrailingGroupPosition)

    /**
     * @return the position of the atom in world space
     */
    fun getPos(): Vector2d

    fun setPos(xPos: Double, yPos: Double)

    fun getFormalCharge(): Int

    fun setFormalCharge(fc: Int)

    fun getMolecule(): MgxMolecule

    /**
     * @return the atom type string (examples: sp3, sp2, ....)
     */
    fun getAtomTypeString(): String

    fun isAtomTypeResolved(): Boolean

    fun getImplicitHCount(): Int

    fun createFormalChargeAt(xPos: Double, yPos: Double): MgxFormalCharge

    /**
     * Since an atom only needs one coordinate to describe itself
     * this returns a map with one entry
     */
    override fun getAllCoordinates(): Map<Int, Vector2d> {
        return mapOf(MAIN_ATOM to getPos())
    }

    override fun pushNewCoordinates(coordinateMap: Map<Int, Vector2d>) {
        val xyz = coordinateMap[MAIN_ATOM]
        if (xyz != null) {
            setPos(xyz.x, xyz.y)
        }
    }

    /**
     * Since an atom only needs one coordinate to describe itself
     * this returns a map with one entry
     */
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