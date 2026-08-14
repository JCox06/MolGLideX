package uk.co.jcox.molglide.control.actions

import org.openscience.cdk.interfaces.IBond
import uk.co.jcox.molglide.StereoChem
import uk.co.jcox.molglide.control.ChemMolecule
import uk.co.jcox.molglide.control.EditorStateData

class ChangeStereoChemAction (private val chemBond: ChemMolecule.ChemBond, val newStereo: StereoChem) : IDataAction{

    private val molecule = chemBond.molecule
    private val toRestore = chemBond.bond.display

    override fun execute(data: EditorStateData) {
        chemBond.bond.display = newStereo.cdk
    }

    override fun undo(data: EditorStateData) {
        chemBond.bond.display = toRestore
    }
}