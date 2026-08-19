package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.StereoChem
import uk.co.jcox.molglide.editor.model.ChemBond
import uk.co.jcox.molglide.editor.model.ChemMolecule
import uk.co.jcox.molglide.editor.model.EditorStateData

class ChangeStereoChemAction (private val chemBond: ChemBond, val newStereo: StereoChem) : IDataAction{

    private val molecule = chemBond.molecule
    private val toRestore = chemBond.bond.display

    override fun execute(data: EditorStateData) {
        chemBond.bond.display = newStereo.cdk
    }

    override fun undo(data: EditorStateData) {
        chemBond.bond.display = toRestore
    }
}