package uk.co.jcox.molglide.editor.model.chemengine

import org.joml.Vector2d
import java.util.Collections

/**
 * Represents a bond in MolGLide that can be placed within the editor
 * It contains chemical data and instructions on how the renderer (UIDataBuilder) should
 * build the UI
 *
 * The Mgx selection of classes are implementation independent of the underlying chemical structure model
 *
 * Get an instance of this class from the associated MgxMolecule
 */
interface MgxBond : ISpatialInfo, IEditorSelectable {

    /**
     * @return true if the bond is at the edge of a chain
     */
    fun isTerminal(): Boolean

    /**
     * @return the vector position midpoint between the two atoms that make up this bond
     */
    fun midpoint(): Vector2d

    fun getStereo(): Stereo

    fun getOrder(): Int

    fun setStereo(newStereo: Stereo)

    /**
     * The UIDataBuilder decides when it's building the UI which side the double bond should go on
     * this tells the UIDataBuilder to reverse the decision it has made
     */
    fun shouldFlip(): Boolean

    fun setFlip(flip: Boolean)

    fun getStart(): MgxAtom

    fun getEnd(): MgxAtom

    fun getMolecule(): MgxMolecule

    /**
     * @return true if the atom is part of this bond
     */
    fun contains(mgxAtom: MgxAtom): Boolean

    override fun pushNewCoordinates(coordinateMap: Map<Int, Vector2d>) {}

    override fun getAllCoordinates(): Map<Int, Vector2d> {
        return mapOf()
    }

    fun setBondAromaticity(aromatic: Boolean)

    fun getBondAromaticity(): Boolean

    enum class Stereo {
        WEDGED,
        HASHED,
        NORMAL
    }
}