package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.chemengine.MgxMolecule
import uk.co.jcox.molglide.editor.model.util.EditorPositionSnapshot

class CleanupStructureAction (private val mgxMolecule: MgxMolecule) : IDataAction {

    private val atomSnapshot = EditorPositionSnapshot.ofMolecule(mgxMolecule)

    override fun execute(data: EditorStateData) {
        mgxMolecule.clean2DStructure()
    }

    override fun undo(data: EditorStateData) {
        atomSnapshot.applyAll()
    }
}