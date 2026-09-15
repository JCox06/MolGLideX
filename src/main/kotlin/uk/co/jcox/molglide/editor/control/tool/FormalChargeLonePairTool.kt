package uk.co.jcox.molglide.editor.control.tool

import jdk.javadoc.internal.doclets.formats.html.resources.standard
import org.apache.jena.sparql.function.library.date
import org.apache.jena.vocabulary.TestManifest.action
import org.joml.Vector2d
import uk.co.jcox.molglide.EditMode
import uk.co.jcox.molglide.editor.control.ActionManager
import uk.co.jcox.molglide.editor.model.SelectionManager
import uk.co.jcox.molglide.IMainAppData
import uk.co.jcox.molglide.editor.control.EventContext
import uk.co.jcox.molglide.editor.control.actions.CompoundAction
import uk.co.jcox.molglide.editor.control.actions.CreateFormalCharge
import uk.co.jcox.molglide.editor.control.actions.DecrementFormalChargeAction
import uk.co.jcox.molglide.editor.control.actions.IDataAction
import uk.co.jcox.molglide.editor.control.actions.IncrementFormalChargeAction
import uk.co.jcox.molglide.editor.model.ChemArrow
import uk.co.jcox.molglide.editor.model.ChemAtom
import uk.co.jcox.molglide.editor.model.ChemFormalCharge
import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.IEditorSelectable

class FormalChargeLonePairTool(val globalContext: IMainAppData, actionManager: ActionManager,
                               selectionManager: SelectionManager, val stateData: EditorStateData) : Tool(actionManager, selectionManager) {


    override fun onClick(clickX: Int, clickY: Int, eventContext: EventContext) {
        val selection = selectionManager.primarySelection
        val selectable = selection?.selectable

        if (selectable is ChemAtom) {
            handleChemAtomClick(selectable, clickX, clickY)
            return
        }
        if (selectable is ChemFormalCharge) {
            val action = handleChargeClick(selectable)
            actionManager.executeAction(action)
            return
        }
    }

    private fun handleChemAtomClick(chemAtom: ChemAtom, clickX: Int, clickY: Int) {
        //First check to see if a formal charge already exists
        val charge = stateData.getCharges().find { it.chemAtom == chemAtom }
        if ( charge != null) {
            //Just handle charge click
            val click = handleChargeClick(charge)
            actionManager.executeAction(click)
            return
        }

        println("It looks like the charge is null")
        //Otherwise create a formal charge, and place it at the mouse click:
        val fc = ChemFormalCharge(Vector2d(clickX.toDouble(), clickY.toDouble()), chemAtom)
        val creation = CreateFormalCharge(fc)
        val modification = handleChargeClick(fc)
        val ca = CompoundAction(creation, modification)
        actionManager.executeAction(ca)
        fc.chemAtom.molecule.calculateAtomProperties()
    }

    private fun handleChargeClick(formalCharge: ChemFormalCharge) : IDataAction {

        if (globalContext.getEditMode() == EditMode.CHARGE_PLUS) {
            return IncrementFormalChargeAction(formalCharge.chemAtom)
        }
        return DecrementFormalChargeAction(formalCharge.chemAtom)
    }

    override fun onRelease(clickX: Int, clickY: Int, eventContext: EventContext) {

    }

    override fun onDragMouse(clickX: Int, clickY: Int, dx: Double, dy: Double, eventContext: EventContext) {

    }

    override fun onSuddenMove() {

    }

    override fun isTypeValidPrimarySelection(selectionContext: SelectionManager.SelectionInfo): Boolean {
        return (selectionContext.selectable is ChemAtom || selectionContext.selectable is ChemFormalCharge)
    }
}