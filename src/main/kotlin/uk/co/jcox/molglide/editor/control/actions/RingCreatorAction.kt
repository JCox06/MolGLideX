package uk.co.jcox.molglide.editor.control.actions

import org.apache.commons.lang3.math.NumberUtils.toDouble
import uk.co.jcox.molglide.EditMode
import uk.co.jcox.molglide.MolGLideUtils
import uk.co.jcox.molglide.editor.EditorConstants
import uk.co.jcox.molglide.editor.control.tool.AtomBondTool
import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.chemengine.MgxMolecule
import uk.co.jcox.molglide.editor.model.chemengine.MgxTemplateBuilder

class RingCreatorAction (
    private val clickX: Int,
    private val clickY: Int,
    private val insert: EditMode,
) : IDataAction {

    lateinit var mgxMolecule: MgxMolecule
    private lateinit var mgxTemplateBuilder: MgxTemplateBuilder

    override fun execute(data: EditorStateData) {
        mgxMolecule = MolGLideUtils.createMolecule()
        mgxTemplateBuilder = mgxMolecule.getTemplateBuilder()

        if (insert == EditMode.RING_BENZENE) {
            mgxTemplateBuilder.buildIsolatedBenzene(clickX.toDouble(), clickY.toDouble(), EditorConstants.DEFAULT_BOND_DISTANCE)
        } else {
            mgxTemplateBuilder.buildIsolatedOrganicRing(insert.ringSize, clickX.toDouble(), clickY.toDouble(), EditorConstants.DEFAULT_BOND_DISTANCE)
        }
        data.addMolecule(mgxMolecule)
    }

    override fun undo(data: EditorStateData) {
        data.removeMolecule(mgxMolecule)
    }

    override fun redo(data: EditorStateData) {
        data.addMolecule(mgxMolecule)
    }
}