package uk.co.jcox.molglide.editor.model

import org.joml.Vector2d
import org.openscience.cdk.interfaces.IAtom
import uk.co.jcox.molglide.editor.model.ChemMolecule.Companion.IGNORE_ERRORS
import uk.co.jcox.molglide.editor.model.ChemMolecule.Companion.TRAILING_POS
import uk.co.jcox.molglide.editor.model.ChemMolecule.Companion.VISIBLE
import uk.co.jcox.molglide.editor.model.ChemMolecule.TrailingGroupPosition
import java.util.*

class ChemAtom (
    val atom: IAtom,
    val molecule: ChemMolecule,
) : IEditorSelectable, ISpatialInfo, MolGLideChemData(atom) {


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
    fun hydrogenLocked(): Boolean = atom.getProperty<Boolean>(ChemMolecule.LOCK_HYDROGEN)

    fun setHydrogenLock(lock: Boolean) {
        atom.setProperty(ChemMolecule.LOCK_HYDROGEN, lock)
    }
    fun getImplicitHCount(): Int {
        return atom.implicitHydrogenCount
    }
    fun setSymbol(symbol: String) {
        atom.symbol = symbol
    }

    fun symbolOverride(): String = atom.getProperty<String>(ChemMolecule.SYMBOL_OVERRIDE)

    fun setSymbolOverride(override: String, chemData: ChemMolecule?) {
        //First check to see if override group is present
        if (this.hasOverrideID()) {
            removeSymbolOverride()
        }

        //Then, remove the anchor atom
        atom.setProperty(ChemMolecule.SYMBOL_OVERRIDE, override)
        this.setVisible(true)
        if (chemData != null) {
            //Mark the chem data
            val markerID = UUID.randomUUID().toString()
            chemData.applyOverrideProperty(markerID)
            setOverrideID(markerID)

            molecule.addChemData(chemData)
            molecule.formBasicConnection(this, ChemAtom(chemData.atoms().first().atom, molecule))
        }
    }

    fun removeSymbolOverride(visibility: Boolean = true) {
        atom.setProperty(ChemMolecule.SYMBOL_OVERRIDE, "")
        this.setVisible(visibility)
        if (!atom.properties.contains(ChemMolecule.OVERRIDE_MARKER)) {
            return
        }
        val removalID = atom.getProperty<String>(ChemMolecule.OVERRIDE_MARKER)

        val bondRemoval = molecule.bonds().filter { hasRequiredMarkerID(it, removalID) }
        val atomRemoval = molecule.atoms().filter { hasRequiredMarkerID(it, removalID, this) }

        bondRemoval.forEach { bond -> this.molecule.removeConnection(bond) }
        atomRemoval.forEach { atom -> this.molecule.removeAtom(atom) }

        atom.setProperty(ChemMolecule.OVERRIDE_MARKER, "")
    }


    private fun hasRequiredMarkerID(chemObject: MolGLideChemData, id: String, ignore: ChemAtom? = null): Boolean {
        if (ignore != null && chemObject == ignore) {
            return false
        }
        return chemObject.hasOverrideID() && chemObject.getOverrideID() == id
    }

    /**
     * If no override is present, the actual symbol is returned
     * If an override is present, the override is returned
     */
    fun getAppliedText(): String {
        if (symbolOverride() == "") {
            return element()
        }
        return symbolOverride()
    }

    fun element(): String = atom.symbol
    fun setTrailPos(trail: TrailingGroupPosition) {
        atom.setProperty(TRAILING_POS, trail)
    }
    fun getTrailPos() : TrailingGroupPosition {
        return atom.getProperty<TrailingGroupPosition>(TRAILING_POS)
    }
    fun getPos() : Vector2d {
        val p2d = atom.point2d ?: return Vector2d()
        return Vector2d(p2d.x, p2d.y)
    }

    fun shouldShowTrailPos(): Boolean {
        return symbolOverride() == ""
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

    fun skipUIBuild(): Boolean {
        return atom.getProperty(ChemMolecule.SKIP_UI_BUILD)
    }

    fun setSkipUIBuild(skip: Boolean) {
        return atom.setProperty(ChemMolecule.SKIP_UI_BUILD, skip)
    }

    companion object {
        const val MAIN_ATOM = 0
        const val CHARGE = 1
    }
}