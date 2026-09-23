package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.StereoChem
import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.chemengine.MgxBond

class ChangeStereoChemAction (private val chemBond: MgxBond, val newStereo: MgxBond.Stereo) : IDataAction{

    private val molecule = chemBond.getMolecule()
    private val toRestore = chemBond.getStereo()

    override fun execute(data: EditorStateData) {
        chemBond.setStereo(newStereo)
    }

    override fun undo(data: EditorStateData) {
        chemBond.setStereo(toRestore)
    }
}