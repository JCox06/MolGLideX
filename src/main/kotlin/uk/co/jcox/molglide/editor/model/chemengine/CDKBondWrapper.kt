package uk.co.jcox.molglide.editor.model.chemengine

import org.joml.Vector2d
import org.joml.Vector2dc
import org.openscience.cdk.interfaces.IAtom
import org.openscience.cdk.interfaces.IBond
import org.openscience.cdk.isomorphism.AtomMappingTools
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator


class CDKBondWrapper (private val cdkBond: IBond, private val mgxMolecule: CDKContainerWrapper) : MgxBond {

    override fun isTerminal(): Boolean {
        val atomA = getStart()
        val atomB = getEnd()

        if (mgxMolecule.bondCount(atomA) > 1 || mgxMolecule.bondCount(atomB) > 1) {
            return false
        }
        return true
    }

    override fun midpoint(): Vector2d {
        val atomA = getStart().getPos()
        val atomB = getEnd().getPos()
        return Vector2d((atomA.x + atomB.x) /2, (atomA.y + atomB.y) /2)
    }

    override fun getStereo(): MgxBond.Stereo {
        val cdkType = cdkBond.display
        return when (cdkType) {
            IBond.Display.WedgeBegin -> MgxBond.Stereo.WEDGED
            IBond.Display.WedgedHashBegin -> MgxBond.Stereo.HASHED
            else -> MgxBond.Stereo.NORMAL
        }
    }

    override fun setStereo(newStereo: MgxBond.Stereo) {
        val cdkType = when (newStereo) {
            MgxBond.Stereo.WEDGED -> IBond.Display.WedgeBegin
            MgxBond.Stereo.HASHED -> IBond.Display.WedgedHashBegin
            MgxBond.Stereo.NORMAL -> IBond.Display.Solid
        }
        cdkBond.display = cdkType
    }

    override fun shouldFlip(): Boolean {
        return cdkBond.getProperty(SHOULD_FLIP) ?: false
    }

    override fun setFlip(flip: Boolean) {
        cdkBond.setProperty(SHOULD_FLIP, flip)
    }

    override fun getStart(): MgxAtom {
        return CDKAtomWrapper(cdkBond.begin, mgxMolecule)
    }

    override fun getEnd(): MgxAtom {
        return CDKAtomWrapper(cdkBond.end, mgxMolecule)
    }

    override fun getMolecule(): MgxMolecule {
        return mgxMolecule
    }

    override fun setTransient(transient: Boolean) {
        cdkBond.setProperty(BOND_TRANSIENT, transient)
    }

    override fun isTransient(): Boolean {
        return cdkBond.getProperty(BOND_TRANSIENT) ?: false
    }

    override fun hashCode(): Int {
        return cdkBond.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is CDKBondWrapper && this.cdkBond == other.cdkBond
    }


    /**
     * For the internal use only between CDK-like classes
     * @return direct access to the underlying CDK object
     */
    fun getHandle() : IBond {
        return cdkBond
    }

    override fun contains(mgxAtom: MgxAtom): Boolean {
        require(mgxAtom is CDKAtomWrapper)
        return cdkBond.contains(mgxAtom.getHandle())
    }

    override fun getObjectSelectionPoints(): Map<Int, Vector2dc> {
        return mapOf(SELECTION_BOND to midpoint())
    }

    override fun setBondAromaticity(aromatic: Boolean) {
        cdkBond.setIsAromatic(aromatic)
    }

    override fun getBondAromaticity(): Boolean {
        return cdkBond.isAromatic
    }

    override fun getOrder(): Int {
        return getMgxOrder(cdkBond.order)
    }

    companion object {
        private const val SHOULD_FLIP = "MOLGLIDE_SHOULD_FLIP"
        private const val BOND_TRANSIENT = "MOLGLIDE_BOND_TRANSIENT"

        private const val SELECTION_BOND = 0

        fun getCDKOrder(mgxOrder: Int): IBond.Order {
            return when (mgxOrder) {
                1 -> IBond.Order.SINGLE
                2 -> IBond.Order.DOUBLE
                3 -> IBond.Order.TRIPLE
                else -> IBond.Order.SINGLE
            }
        }

        fun getMgxOrder(cdkOrder: IBond.Order): Int {
            return when (cdkOrder) {
                IBond.Order.SINGLE -> 1
                IBond.Order.DOUBLE -> 2
                IBond.Order.TRIPLE -> 3
                else -> 1
            }
        }
    }

}