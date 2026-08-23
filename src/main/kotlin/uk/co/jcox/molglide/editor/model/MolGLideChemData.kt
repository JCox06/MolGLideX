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

    companion object {
        const val TEMP_MARKER_KEY: String = "MOLGLIDE_TEMP_MARKER"
    }
}