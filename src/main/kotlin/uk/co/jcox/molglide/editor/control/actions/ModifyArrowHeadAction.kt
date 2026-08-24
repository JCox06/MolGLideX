package uk.co.jcox.molglide.editor.control.actions

import uk.co.jcox.molglide.editor.model.ChemArrow
import uk.co.jcox.molglide.editor.model.EditorStateData

class ModifyArrowHeadAction (private val chemArrow: ChemArrow, private val selection: Int, private val arrowType: ChemArrow.ArrowHead): IDataAction {

    private var oldArrowType: ChemArrow.ArrowHead
    init {
        oldArrowType = chemArrow.startArrow
        if (selection == ChemArrow.START) oldArrowType = chemArrow.startArrow
        if (selection == ChemArrow.END) oldArrowType = chemArrow.endArrow
    }

    override fun execute(data: EditorStateData) {
        if (selection == ChemArrow.START) {
            chemArrow.startArrow = arrowType
        }
        if (selection == ChemArrow.END) {
            chemArrow.endArrow = arrowType
        }
    }

    override fun undo(data: EditorStateData) {
        if (selection == ChemArrow.START) {
            chemArrow.startArrow = oldArrowType
        }
        if (selection == ChemArrow.END) {
            chemArrow.endArrow = oldArrowType
        }
    }
}