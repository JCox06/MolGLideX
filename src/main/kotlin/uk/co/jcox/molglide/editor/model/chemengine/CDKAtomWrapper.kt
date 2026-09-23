package uk.co.jcox.molglide.editor.model.chemengine

import org.joml.Vector2d
import org.openscience.cdk.interfaces.IAtom
import javax.vecmath.Point2d

class CDKAtomWrapper (private val cdkAtom: IAtom, private val mgxMolecule: CDKContainerWrapper) : MgxAtom {

    override val isCarbon: Boolean
        get() = cdkAtom.symbol == "C"

    override fun isNotImplicit(): Boolean {
        return cdkAtom.getProperty(IS_NOT_IMPLICIT) ?: false
    }

    override fun setNotImplicit(implicit: Boolean) {
        cdkAtom.setProperty(IS_NOT_IMPLICIT, implicit)
    }

    override fun ignoreErrors(): Boolean {
        return cdkAtom.getProperty(IGNORE_ERRORS) ?: false
    }

    override fun setIgnoreErrors(ignore: Boolean) {
        cdkAtom.setProperty(IGNORE_ERRORS, ignore)
    }

    override fun getTrailPos(): MgxAtom.TrailingGroupPosition {
        return cdkAtom.getProperty(TRAIL_POS) ?: MgxAtom.TrailingGroupPosition.RIGHT
    }

    override fun setTrailPos(pos: MgxAtom.TrailingGroupPosition) {
        cdkAtom.setProperty(TRAIL_POS, pos)
    }

    override fun getPos(): Vector2d {
        val pos = cdkAtom.point2d ?: Point2d(0.0, 0.0)
        return Vector2d(pos.x, pos.y)
    }

    override fun setPos(xPos: Double, yPos: Double) {
        cdkAtom.point2d = Point2d(xPos, yPos)
    }

    override fun getFormalCharge(): Int {
        return cdkAtom.formalCharge
    }

    override fun setFormalCharge(fc: Int) {
        cdkAtom.formalCharge = fc
    }

    override fun getMolecule(): MgxMolecule {
        return mgxMolecule
    }

    override fun getAllCoordinates(): Map<Int, Vector2d> {
        return super.getAllCoordinates()
    }

    override fun setTransient(transient: Boolean) {
        cdkAtom.setProperty(IS_TRANSIENT, transient)
    }

    override fun isTransient(): Boolean {
        return cdkAtom.getProperty(IS_TRANSIENT) ?: false
    }

    override fun hashCode(): Int {
        return cdkAtom.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is CDKAtomWrapper && this.cdkAtom == other.cdkAtom
    }

    /**
     * For the internal use only between CDK-like classes
     * @return direct access to the underlying CDK object
     */
    fun getHandle() : IAtom {
        return cdkAtom
    }

    override fun getSymbol(): String {
        return cdkAtom.symbol
    }

    override fun getAtomTypeString(): String {
        val name = cdkAtom.atomTypeName ?: ""
        return name
    }

    override fun isAtomTypeResolved(): Boolean {
        return cdkAtom.atomTypeName == ATOM_TYPE_UNKNOWN || cdkAtom.atomTypeName == null
    }

    override fun getImplicitHCount(): Int {
        val hCount = cdkAtom.implicitHydrogenCount ?: return 0
        return hCount
    }

    override fun createFormalChargeAt(xPos: Double, yPos: Double): MgxFormalCharge {
        return FormalChargeWrapper(this, xPos, yPos)
    }

    companion object {
        private const val IS_NOT_IMPLICIT = "MOLGLIDE_NOT_IMPLICIT"
        private const val IGNORE_ERRORS = "MOLGLIDE_IGNORE_ERRORS"
        private const val TRAIL_POS = "MOLGLIDE_TRAIL_POS_HINT"
        private const val IS_TRANSIENT = "MOLGLIDE_TRANSIENT_RENDER"

        private const val ATOM_TYPE_UNKNOWN = "X"
    }
}