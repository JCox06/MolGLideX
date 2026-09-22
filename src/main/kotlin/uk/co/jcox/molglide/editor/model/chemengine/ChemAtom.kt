package uk.co.jcox.molglide.editor.model.chemengine

import org.joml.Vector2d
import org.openscience.cdk.interfaces.IAtom
import uk.co.jcox.molglide.editor.model.IEditorSelectable
import uk.co.jcox.molglide.editor.model.ISpatialInfo
import uk.co.jcox.molglide.editor.model.MolGLideChemData

class ChemAtom (
    val atom: IAtom,
    val molecule: ChemMolecule,
) : IEditorSelectable, ISpatialInfo, MolGLideChemData(atom) {
    fun isVisible(): Boolean {
        return atom.getProperty<Boolean>(ChemMolecule.VISIBLE)
    }
    fun setVisible(visible: Boolean) {
        atom.setProperty(ChemMolecule.VISIBLE, visible)
    }
    fun shouldIgnoreErrors(): Boolean {
        return atom.getProperty(ChemMolecule.IGNORE_ERRORS)
    }
    fun setIgnoreErrors(ignore: Boolean) {
        atom.setProperty(ChemMolecule.IGNORE_ERRORS, ignore)
    }
    fun isCarbon(): Boolean {
        return atom.symbol == "C"
    }
    fun setTrailPos(trail: ChemMolecule.TrailingGroupPosition) {
        atom.setProperty(ChemMolecule.TRAILING_POS, trail)
    }
    fun getTrailPos() : ChemMolecule.TrailingGroupPosition {
        return atom.getProperty<ChemMolecule.TrailingGroupPosition>(ChemMolecule.TRAILING_POS)
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

    override fun getObjectSelectionPoints(): Map<Int, Vector2d> {
        val map = mutableMapOf<Int, Vector2d>()
        map[MAIN_ATOM] = getPos()
        return map
    }

    override fun getAllCoordinates(): Map<Int, Vector2d> {
        return getObjectSelectionPoints()
    }

    override fun pushNewCoordinates(coordinateMap: Map<Int, Vector2d>) {
        val newPos = coordinateMap[0] ?: return
        setPos(newPos)
    }

    fun getFormalCharge(): Int {
        return atom.formalCharge
    }

    fun setFormalCharge(newCharge: Int) {
        atom.formalCharge = newCharge
        molecule.calculateAtomProperties()
    }


    fun setPos(vector: Vector2d) {
        atom.point2d.x = vector.x
        atom.point2d.y = vector.y
    }


    companion object {
        const val MAIN_ATOM = 0
        const val CHARGE = 1
    }
}