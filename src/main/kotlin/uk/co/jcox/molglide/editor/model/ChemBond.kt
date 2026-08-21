package uk.co.jcox.molglide.editor.model

import org.joml.Vector2d
import org.openscience.cdk.interfaces.IBond
import uk.co.jcox.molglide.StereoChem
import uk.co.jcox.molglide.editor.model.ChemMolecule.Companion.FLIP_BOND

class ChemBond (
    val bond: IBond,
    val molecule: ChemMolecule,
) : IEditorSelectable, MolGLideChemData(bond) {
    override fun equals(other: Any?): Boolean {
        return other is ChemBond && this.bond == other.bond
    }

    override fun hashCode(): Int {
        return bond.hashCode()
    }

    fun isTerminal(): Boolean {
        val atomA = bond.begin
        val atomB = bond.end

        if (atomA.bondCount > 1 || atomB.bondCount > 1) {
            return false
        }
        return true
    }

    fun midPoint(): Vector2d {
        val atomA = bond.begin.point2d
        val atomB = bond.end.point2d
        return Vector2d((atomA.x + atomB.x) / 2, (atomA.y + atomB.y) /2)
    }

    fun stereo(): StereoChem {
        return StereoChem.getType(bond.display)
    }
    fun setStereo(s: StereoChem) {
        val cdkType = s.cdk
        bond.display = cdkType
    }
    fun shouldFlip(): Boolean {
        return bond.getProperty<Boolean>(FLIP_BOND)
    }
    fun setFlip(flip: Boolean) {
        bond.setProperty(FLIP_BOND, flip)
    }
    fun getStart(): ChemAtom {
        return ChemAtom(bond.begin, molecule)
    }
    fun getEnd(): ChemAtom {
        return ChemAtom(bond.end, molecule)
    }
    override fun isTransient(): Boolean {
        return super.isTransient() || molecule.isTransient()
    }

    override fun getSelectionPosition(): Vector2d {
        return midPoint()
    }
}