package uk.co.jcox.molglide.editor.control.tool

import org.joml.Vector2d
import uk.co.jcox.molglide.EditMode
import uk.co.jcox.molglide.IMainAppData
import uk.co.jcox.molglide.editor.control.ActionManager
import uk.co.jcox.molglide.editor.control.EventContext
import uk.co.jcox.molglide.editor.control.actions.CreateFormalCharge
import uk.co.jcox.molglide.editor.control.actions.DecrementFormalChargeAction
import uk.co.jcox.molglide.editor.control.actions.IDataAction
import uk.co.jcox.molglide.editor.control.actions.IncrementFormalChargeAction
import uk.co.jcox.molglide.editor.model.chemengine.FormalChargeWrapper
import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.SelectionManager
import uk.co.jcox.molglide.editor.model.chemengine.MgxAtom
import uk.co.jcox.molglide.editor.model.chemengine.MgxFormalCharge

class FormalChargeLonePairTool(val globalContext: IMainAppData, actionManager: ActionManager,
                               selectionManager: SelectionManager, val stateData: EditorStateData) : Tool(actionManager, selectionManager) {


    override fun onClick(clickX: Int, clickY: Int, eventContext: EventContext) {
        val selection = selectionManager.primarySelection
        val selectable = selection?.selectable

        if (selectable is MgxAtom) {
            handleChemAtomClick(selectable, clickX, clickY)
            return
        }
        if (selectable is FormalChargeWrapper) {
            val action = handleChargeClick(selectable)
            actionManager.executeAction(action)
            return
        }
    }

    private fun handleChemAtomClick(chemAtom: MgxAtom, clickX: Int, clickY: Int) {
        //First check to see if a formal charge already exists
        val charge = stateData.getCharges().find { it.getAssociatedAtom() == chemAtom }
        if ( charge != null) {
            //Just handle charge click
            val click = handleChargeClick(charge)
            actionManager.executeAction(click)
            return
        }
        //Otherwise create a formal charge, and place it at the mouse click:
        val fc = chemAtom.createFormalChargeAt(clickX.toDouble(), clickY.toDouble())
        var defaultCharge = 1
        if (globalContext.getEditMode() == EditMode.CHARGE_NEGATIVE) {
            defaultCharge = -1
        }
        fc.setCharge(defaultCharge)
        val creation = CreateFormalCharge(fc)
        actionManager.executeAction(creation)
        fc.getAssociatedAtom().getMolecule().calculateChemData()
    }

    private fun handleChargeClick(formalCharge: MgxFormalCharge) : IDataAction {

        if (globalContext.getEditMode() == EditMode.CHARGE_PLUS) {
            return IncrementFormalChargeAction(formalCharge.getAssociatedAtom())
        }
        return DecrementFormalChargeAction(formalCharge.getAssociatedAtom())
    }

    override fun onRelease(clickX: Int, clickY: Int, eventContext: EventContext) {

    }

    override fun onDragMouse(clickX: Int, clickY: Int, dx: Double, dy: Double, eventContext: EventContext) {

    }

    override fun onSuddenMove() {

    }

    override fun isTypeValidPrimarySelection(selectionContext: SelectionManager.SelectionInfo): Boolean {
        return (selectionContext.selectable is MgxAtom || selectionContext.selectable is FormalChargeWrapper)
    }
}