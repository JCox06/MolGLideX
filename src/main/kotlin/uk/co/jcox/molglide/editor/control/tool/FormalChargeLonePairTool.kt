package uk.co.jcox.molglide.editor.control.tool

import uk.co.jcox.molglide.EditMode
import uk.co.jcox.molglide.editor.control.ActionManager
import uk.co.jcox.molglide.editor.model.SelectionManager
import uk.co.jcox.molglide.IMainAppData
import uk.co.jcox.molglide.editor.control.EventContext
import uk.co.jcox.molglide.editor.control.actions.DecrementFormalChargeAction
import uk.co.jcox.molglide.editor.control.actions.IncrementFormalChargeAction
import uk.co.jcox.molglide.editor.model.ChemArrow
import uk.co.jcox.molglide.editor.model.ChemAtom
import uk.co.jcox.molglide.editor.model.IEditorSelectable

class FormalChargeLonePairTool(val globalContext: IMainAppData, actionManager: ActionManager,
                               selectionManager: SelectionManager) : Tool(actionManager, selectionManager) {


    override fun onClick(clickX: Int, clickY: Int, eventContext: EventContext) {
        val chemAtom = selectionManager.getAtom() ?: return

        if (globalContext.getEditMode() == EditMode.CHARGE_PLUS) {
            val action = IncrementFormalChargeAction(chemAtom)
            actionManager.executeAction(action)
        }
        if (globalContext.getEditMode() == EditMode.CHARGE_NEGATIVE) {
            val action = DecrementFormalChargeAction(chemAtom)
            actionManager.executeAction(action)
        }

    }

    override fun onRelease(clickX: Int, clickY: Int, eventContext: EventContext) {

    }

    override fun onDragMouse(clickX: Int, clickY: Int, dx: Double, dy: Double, eventContext: EventContext) {

    }

    override fun onSuddenMove() {

    }

    override fun isTypeValidPrimarySelection(selectionContext: SelectionManager.SelectionInfo): Boolean {
        return (selectionContext.selectable is ChemAtom)
    }
}