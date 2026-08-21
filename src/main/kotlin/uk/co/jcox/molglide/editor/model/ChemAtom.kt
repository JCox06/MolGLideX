package uk.co.jcox.molglide.editor.model

import org.joml.Vector2d
import org.openscience.cdk.interfaces.IAtom
import uk.co.jcox.molglide.editor.model.ChemMolecule.Companion.IGNORE_ERRORS
import uk.co.jcox.molglide.editor.model.ChemMolecule.Companion.TRAILING_POS
import uk.co.jcox.molglide.editor.model.ChemMolecule.Companion.VISIBLE
import uk.co.jcox.molglide.editor.model.ChemMolecule.TrailingGroupPosition

class ChemAtom (
    val atom: IAtom,
    val molecule: ChemMolecule,
) : IEditorSelectable, MolGLideChemData(atom) {
    fun isVisible(): Boolean {
        return atom.getProperty<Boolean>(VISIBLE)
    }
    fun setVisible(visible: Boolean) {
        atom.setProperty(VISIBLE, visible)
    }
    fun shouldIgnoreErrors(): Boolean {
        return atom.getProperty(IGNORE_ERRORS)
    }
    fun setIgnoreErrors(ignore: Boolean) {
        atom.setProperty(IGNORE_ERRORS, ignore)
    }
    fun isCarbon(): Boolean {
        return atom.symbol == "C"
    }
    fun setTrailPos(trail: TrailingGroupPosition) {
        atom.setProperty(TRAILING_POS, trail)
    }
    fun getTrailPos() : TrailingGroupPosition {
        return atom.getProperty<TrailingGroupPosition>(TRAILING_POS)
    }
    fun getPos() : Vector2d {
        val p2d = atom.point2d
        return Vector2d(p2d.x, p2d.y)
    }

    override fun hashCode(): Int {
        return atom.hashCode()
    }
    override fun equals(other: Any?): Boolean {
        return other is ChemAtom && this.atom == other.atom
    }

    override fun isTransient(): Boolean {
        return super.isTransient() || molecule.isTransient()
    }

    override fun getSelectionPosition(): Vector2d {
        return getPos()
    }
}