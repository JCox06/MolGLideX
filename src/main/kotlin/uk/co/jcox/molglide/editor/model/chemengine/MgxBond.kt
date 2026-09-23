package uk.co.jcox.molglide.editor.model.chemengine

import org.joml.Vector2d
import java.util.Collections

interface MgxBond : ISpatialInfo, IEditorSelectable {

    fun isTerminal(): Boolean

    fun midpoint(): Vector2d

    fun getStereo(): Stereo

    fun getOrder(): Int

    fun setStereo(newStereo: Stereo)

    fun shouldFlip(): Boolean

    fun setFlip(flip: Boolean)

    fun getStart(): MgxAtom

    fun getEnd(): MgxAtom

    fun getMolecule(): MgxMolecule

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