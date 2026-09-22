package uk.co.jcox.molglide.editor.model

import org.openscience.cdk.interfaces.IChemObject
import uk.co.jcox.molglide.editor.model.ChemMolecule.Companion.TRANSIENT

open class MolGLideChemData(
    /**
     * Indicates to the renderer that this atom/bond/ may update every frame.
     *
     * When applied to the molecule, any bond/atom in that molecule also becomes transient
     */
    private val cdkObject: IChemObject,

    ) : IChemComponent {
    override fun isTransient(): Boolean {
        return cdkObject.getProperty(TRANSIENT)
    }
    override fun setTransient(value: Boolean) {
        cdkObject.setProperty(TRANSIENT, value)
    }
    fun setOverrideID(id: String) {
        cdkObject.setProperty(ChemMolecule.OVERRIDE_MARKER, id)
    }
    fun getOverrideID(): String {
        return cdkObject.getProperty(ChemMolecule.OVERRIDE_MARKER)
    }
    fun hasOverrideID(): Boolean {
        return cdkObject.properties.contains(ChemMolecule.OVERRIDE_MARKER)
    }

    open fun internalOnly(): Boolean {
        if (!cdkObject.properties.contains(ChemMolecule.SKIP_UI_BUILD)) {
            return false
        }
        return cdkObject.getProperty<Boolean>(ChemMolecule.SKIP_UI_BUILD)
    }

    open fun setInternalOnly(internal: Boolean) {
        cdkObject.setProperty(ChemMolecule.SKIP_UI_BUILD, internal)
    }

    companion object {
        const val TEMP_MARKER_KEY: String = "MOLGLIDE_TEMP_MARKER"
    }
}