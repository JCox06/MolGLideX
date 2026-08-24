package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.util.EditorPositionSnapshot

class MoveSpatialAction (private val newPositions: EditorPositionSnapshot, private val oldPositions: EditorPositionSnapshot) : IDataAction {


    override fun execute(data: EditorStateData) {
        newPositions.applyAll()
    }

    override fun undo(data: EditorStateData) {
        oldPositions.applyAll()
    }
}