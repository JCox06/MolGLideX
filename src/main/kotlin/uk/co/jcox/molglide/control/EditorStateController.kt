package uk.co.jcox.molglide.control

import org.openscience.cdk.interfaces.IBond
import uk.co.jcox.molglide.EditMode
import uk.co.jcox.molglide.StereoChem
import uk.co.jcox.molglide.control.actions.AtomDeletionAction
import uk.co.jcox.molglide.control.actions.BondDeletionAction
import uk.co.jcox.molglide.control.actions.ChangeStereoChemAction
import uk.co.jcox.molglide.control.actions.CompoundAction
import uk.co.jcox.molglide.control.actions.FlipBondAction
import uk.co.jcox.molglide.control.actions.ToggleAtomVisibilityAction
import uk.co.jcox.molglide.control.actions.UpdateBondAromaticityAction
import uk.co.jcox.molglide.control.actions.UpdateBondOrderAction
import uk.co.jcox.molglide.control.tool.AtomBondTool
import uk.co.jcox.molglide.control.tool.TemplateRingTool
import uk.co.jcox.molglide.control.tool.Tool
import uk.co.jcox.molglide.io.LevelSerializer
import uk.co.jcox.molglide.ui.EditorPanel
import java.io.File
import java.nio.file.Files

class EditorStateController (
    private val appManager: AppManager,
    private val stateData : EditorStateData,
) {
    val actionManager: ActionManager = ActionManager(stateData)

    private val selectionManager: SelectionManager = SelectionManager()
    private var currentTool: Tool = AtomBondTool(appManager, actionManager, selectionManager, stateData)

    /**
     * If this document has been saved before, then this is not null
     * If the document has been saved, it returns the file it was saved to
     * (useful in save as, and save operations!)
     *
     * The lastest save as operation will override the previous name!
     */
    var lastUsedSaveFile: File? = null
    private set

    val uiBuilder = UIBuilder(stateData, selectionManager)


    fun handleMouseClick(mouseX: Int, mouseY: Int) {
        prepareTool()
        currentTool.onClick(mouseX, mouseY)
        uiBuilder.rebuild()
    }

    fun handleMouseRelease(mouseX: Int, mouseY: Int) {
        currentTool.onRelease(mouseX, mouseY)
        uiBuilder.rebuild()
    }

    fun handleSuddenMouseMove() {
        currentTool.onSuddenMove()
    }

    fun handleMouseDrag(mouseX: Int, mouseY: Int) {
        currentTool.onDragMouse(mouseX, mouseY)
    }

    private fun prepareTool() {
        if (appManager.editMode.type == EditMode.ToolType.ATOM_INSERT) {
            currentTool = AtomBondTool(appManager, actionManager, selectionManager, stateData)
        }
        if (appManager.editMode.type == EditMode.ToolType.RING_INSERT) {
            currentTool = TemplateRingTool(appManager, actionManager, selectionManager, stateData)
        }
    }

    fun update(worldX: Int, worldY: Int) {
        currentTool.updateMouseWorld(worldX, worldY)
        selectionManager.update(stateData, worldX, worldY)
        if (actionManager.isDirty) {
            uiBuilder.rebuild()
        }
    }

    fun checkSelected(atom: UIAtom) : Boolean {
        val selection = selectionManager.primarySelection
        return selection is SelectionManager.Type.ActiveAtom && selection.chemAtom.atom.id == atom.chemID
    }

    fun nowActive(panel: EditorPanel) {
        appManager.activeTab = this
        appManager.activePanel = panel
    }

    fun deleteSelectedAtom() {
        val atom = selectionManager.getAtom() ?: return
        val action = AtomDeletionAction(atom)
        actionManager.executeAction(action)
    }

    fun toggleSelectedAtomVisiblity() {
        val atom = selectionManager.getAtom() ?: return
        val action = ToggleAtomVisibilityAction(atom)
        actionManager.executeAction(action)

    }

    fun flipSelectedBond() {
        val bond = selectionManager.getBond() ?: return
        val action = FlipBondAction(bond)
        actionManager.executeAction(action)
    }

    fun updateSingleSelectedBond(bondOptions: StereoChem) {
        val bond = selectionManager.getBond() ?: return

        //Again as explained below, I am only allowing single bonds to have stereochem
        val changeOrder = UpdateBondOrderAction(bond, IBond.Order.SINGLE)
        val changeStereo = ChangeStereoChemAction(bond, bondOptions)
        val compoundAction = CompoundAction(changeOrder, changeStereo)
        actionManager.executeAction(compoundAction)
    }

    fun updateDoubleSelectedBond() {
        val bond = selectionManager.getBond() ?: return
        //Although double bonds do have stereochemistry
        //it is not really depicted in chemical sketchers
        //So I think for a simple molecular editor, its okay for the moment to assume
        //double bonds should NOT have stereochemistry information associated with them
        val removeStereoAction = ChangeStereoChemAction(bond, StereoChem.NORMAL)
        val updateBondOrderAction = UpdateBondOrderAction(bond, IBond.Order.DOUBLE)
        val compoundAction = CompoundAction(removeStereoAction, updateBondOrderAction)
        actionManager.executeAction(compoundAction)
    }


    //Like the methods above, it might look first strange to see that this is not a compound action
    //that affects the double bond option too.
    //However in the case of benzene, the CDK recommends to have all the bonds (including formally single bonds)
    //to be set as aromatic
    fun updateAromaticSelectedBond() {
        val bond = selectionManager.getBond() ?: return
        val action = UpdateBondAromaticityAction(bond)
        actionManager.executeAction(action)
    }

    fun invertStereoChemSelectedBond() {
        TODO()
    }

    fun setTripleSelectedBond() {
        val bond = selectionManager.getBond() ?: return
        val action = UpdateBondOrderAction(bond, IBond.Order.TRIPLE)
        actionManager.executeAction(action)
    }

    fun deleteSelectedBond() {
        val selection = selectionManager.primarySelection
        if (selection is SelectionManager.Type.ActiveBond) {
            val action = BondDeletionAction(selection.chemBond)
            actionManager.executeAction(action)
        }
    }

    fun saveProject(file: File) {
        val levelSerializer = LevelSerializer()
        val json = levelSerializer.getJSONEncoding(stateData)
        file.writeText(json)
    }
}