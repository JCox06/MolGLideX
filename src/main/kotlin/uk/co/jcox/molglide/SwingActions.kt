package uk.co.jcox.molglide

import org.openscience.cdk.interfaces.IBond
import uk.co.jcox.molglide.editor.control.EditorStateController
import uk.co.jcox.molglide.editor.model.ChemArrow
import uk.co.jcox.molglide.editor.model.ChemAtom
import uk.co.jcox.molglide.editor.model.ChemBond
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.net.URI
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.KeyStroke

class UndoAction (val mainController: MainController) : MolGLideSwingAction("Undo") {

    init {
        putValue(NAME, "Undo")
        putValue(SHORT_DESCRIPTION, "Undo the last operation")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK))

        isEnabled = false
    }

    override fun actionPerformed(e: ActionEvent?) {
        mainController.handleGlobalUndo()
    }

    override fun chemDataChanged(activeSession: EditorSession, currentBond: ChemBond?, currentAtom: ChemAtom?) {
        isEnabled = activeSession.editorController.actionManager.canUndo()
    }
}

class RedoAction (val mainController: MainController) : MolGLideSwingAction("Redo") {

    init {
        putValue(NAME, "Redo")
        putValue(SHORT_DESCRIPTION, "Redo the last operation")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK))

        isEnabled = false
    }

    override fun actionPerformed(e: ActionEvent?) {
        mainController.handleGlobalRedo()
    }

    override fun chemDataChanged(activeSession: EditorSession, currentBond: ChemBond?, currentAtom: ChemAtom?) {
        isEnabled = activeSession.editorController.actionManager.canRedo()
    }

}

class QuitAction (val mainController: MainController) : MolGLideSwingAction("Quit") {

    init {
        putValue(SHORT_DESCRIPTION, "Quits the Application")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK))
    }

    override fun actionPerformed(e: ActionEvent?) {
        mainController.shutdown()
    }

}


class NewProjectAction (val mainController: MainController) :  MolGLideSwingAction("New MGF Project") {

    init {
        putValue(SHORT_DESCRIPTION, "Create a new MolGLideX project")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK))
    }

    override fun actionPerformed(e: ActionEvent?) {
        mainController.newProject()
    }
}



class EditLabelMenuAction (val panel: JFrame, val getController: () -> EditorStateController?)
    : MolGLideSwingAtomAction("Edit Label") {
    init {
        putValue(SHORT_DESCRIPTION, "Edits the atom label")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0))
    }

    //todo see https://github.com/JCox06/MolGLideX/issues/2
    override fun actionPerformed(e: ActionEvent?) {
        //For now
        val label = JOptionPane.showInputDialog(panel, "Type an element", "Edit Label", JOptionPane.QUESTION_MESSAGE)
        label.trim()
        getController()?.updateAtomLabel(label)
    }
}

class DeleteAtomMenuAction (val getController: () -> EditorStateController?)
    : MolGLideSwingAtomAction("Delete Atom") {
    init {
        putValue(SHORT_DESCRIPTION, "Deletes the atom")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0))
    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.deleteSelectedAtom()
    }
}

class ToggleAtomVisibilityMenuAction (val getController: () -> EditorStateController?
) : MolGLideSwingAtomAction("Atom Visible") {
    init {
        putValue(SHORT_DESCRIPTION, "Select whether this atom should be visible")
    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.toggleSelectedAtomVisibility()
    }

    override fun chemDataChanged(
        activeSession: EditorSession,
        currentBond: ChemBond?,
        currentAtom: ChemAtom?
    ) {
        super.chemDataChanged(activeSession, currentBond, currentAtom)
        putValue(SELECTED_KEY, currentAtom?.isVisible())
    }
}

class FlipBondMenuAction (val getController: () -> EditorStateController?
) : MolGLideSwingBondAction("Flip Double Bond") {
    init {
        putValue(SHORT_DESCRIPTION, "Toggles the side of the double bond")
    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.flipSelectedBond()
    }
}

class SetPlainBondMenuAction (val getController: () -> EditorStateController?)
    : MolGLideSwingBondAction("Plain Bond"
) {
    init {
        putValue(SHORT_DESCRIPTION, "Select single bond")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_1, 0))
    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.updateSingleSelectedBond(StereoChem.NORMAL)
    }

    override fun chemDataChanged(
        activeSession: EditorSession,
        currentBond: ChemBond?,
        currentAtom: ChemAtom?
    ) {
        super.chemDataChanged(activeSession, currentBond, currentAtom)
        putValue(SELECTED_KEY, currentBond?.bond?.order == IBond.Order.SINGLE && currentBond.stereo() == StereoChem.NORMAL)
    }
}

class SetWedgedBondMenuAction (val getController: () -> EditorStateController?) : MolGLideSwingBondAction("Wedged Bond") {
    init {
        putValue(SHORT_DESCRIPTION, "Select wedged bond")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, 0))
    }

    override fun chemDataChanged(activeSession: EditorSession, currentBond: ChemBond?, currentAtom: ChemAtom?) {
        super.chemDataChanged(activeSession, currentBond, currentAtom)
        putValue(SELECTED_KEY, currentBond?.bond?.order == IBond.Order.SINGLE && currentBond.stereo() == StereoChem.WEDGED)
    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.updateSingleSelectedBond(StereoChem.WEDGED)
    }
}

class SetDashedBondMenuAction (val getController: () -> EditorStateController?)
    : MolGLideSwingBondAction("Hashed Bond") {
    init {
        putValue(SHORT_DESCRIPTION, "Select dashed bond")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H, 0))

//        putValue(SELECTED_KEY, isDashed)
    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.updateSingleSelectedBond(StereoChem.DASHED)
    }
    override fun chemDataChanged(activeSession: EditorSession, currentBond: ChemBond?, currentAtom: ChemAtom?) {
        super.chemDataChanged(activeSession, currentBond, currentAtom)
        putValue(SELECTED_KEY, currentBond?.bond?.order == IBond.Order.SINGLE && currentBond.stereo() == StereoChem.DASHED)
    }

}

class FlipStereoChemMenuAction (val getController: () -> EditorStateController?)
    : MolGLideSwingBondAction("Flip Stereo Chem") {
    init {
        putValue(SHORT_DESCRIPTION, "Flip the direction of stereo chem")
    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.invertStereoChemSelectedBond()
    }
}


class SetDoubleBondMenuAction (val getController: () -> EditorStateController?) : MolGLideSwingBondAction("Plain Double") {
    init {
        putValue(SHORT_DESCRIPTION, "Select double bond")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_2, 0))

    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.updateDoubleSelectedBond()
    }

    override fun chemDataChanged(activeSession: EditorSession, currentBond: ChemBond?, currentAtom: ChemAtom?) {
        super.chemDataChanged(activeSession, currentBond, currentAtom)
        putValue(SELECTED_KEY, currentBond?.bond?.order == IBond.Order.DOUBLE)
    }

}

class SetAromaticDoubleBondMenuAction (val getController: () -> EditorStateController?) : MolGLideSwingBondAction("Aromatic Bond"
) {
    init {
        putValue(SHORT_DESCRIPTION, "Select aromatic bond")
    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.updateAromaticSelectedBond()
    }

    override fun chemDataChanged(activeSession: EditorSession, currentBond: ChemBond?, currentAtom: ChemAtom?) {
        super.chemDataChanged(activeSession, currentBond, currentAtom)
        putValue(SELECTED_KEY, currentBond?.bond?.isAromatic)
    }
}

class SetTripleBondMenuAction (val getController: () -> EditorStateController?) : MolGLideSwingBondAction("Triple Bond"
) {
    init {
        putValue(SHORT_DESCRIPTION, "Select Triple bond")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_3, 0))

    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.setTripleSelectedBond()
    }

    override fun chemDataChanged(activeSession: EditorSession, currentBond: ChemBond?, currentAtom: ChemAtom?) {
        super.chemDataChanged(activeSession, currentBond, currentAtom)
        putValue(SELECTED_KEY, currentBond?.bond?.order == IBond.Order.TRIPLE)
    }
}

class DeleteBondMenuAction (val getController: () -> EditorStateController?) : MolGLideSwingBondAction("Delete Bond",
) {
    init {
        putValue(SHORT_DESCRIPTION, "Select Triple bond")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0))

    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.deleteSelectedBond()
    }
}


class SaveFileAction (val mainController: MainController) : MolGLideSwingAction("Save file") {
    init {
        putValue(SHORT_DESCRIPTION, "Saves current progress as .mgx file")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK))

    }

    override fun actionPerformed(e: ActionEvent?) {
        mainController.saveActiveProject()
    }

}


class SaveAsFileAction (val mainController: MainController, val mainFrame: MolGlideFrame) : MolGLideSwingAction("Save as") {
    init {
        putValue(SHORT_DESCRIPTION, "Saves current progress as a new .mgx file")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK))

    }

    override fun actionPerformed(e: ActionEvent?) {
        val location = MolGLideUtils.showSaveDialogue(mainFrame)
        if (location != null) {
            mainController.saveActiveProjectAs(location)
        }
    }
}


class LoadFileAction (val mainController: MainController, val mainFrame: MolGlideFrame) : MolGLideSwingAction("Load file") {

    init {
        putValue(SHORT_DESCRIPTION, "Load a MolGLide project (.mgx file)")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK))
    }

    override fun actionPerformed(e: ActionEvent?) {
        val file = MolGLideUtils.showOpenDialogue()
        if (file != null) {
            mainController.openProject(file)
        }
    }
}


class CopySelectionAction(val mainController: MainController) : MolGLideSwingAction("Copy Selection") {

    init {
        putValue(SHORT_DESCRIPTION, "Copy selected components to the clipboard")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK))
    }

    //The copy selection needs to do two things
    //1) Copy the selected components in SVG format to the clipboard to use in external applications
    //2) Copy the selected components in MGX format to the clipboard to use in MolGLide applications
    override fun actionPerformed(e: ActionEvent?) {
        mainController.copySelectedMolecules()
    }

    override fun chemDataChanged(activeSession: EditorSession, currentBond: ChemBond?, currentAtom: ChemAtom?) {
        isEnabled = activeSession.editorData.selectionManager.hasBatchSelection()
    }
}

class PasteSelectionAction(val mainController: MainController) : MolGLideSwingAction("Paste Selection") {

    private val clipboard = Toolkit.getDefaultToolkit().systemClipboard

    init {
        putValue(SHORT_DESCRIPTION, "Paste selected components from the clipboard")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK))
    }

    override fun actionPerformed(e: ActionEvent?) {
        mainController.pasteSelectedMolecules()
    }
}

class CutSelectionAction(val mainController: MainController) : MolGLideSwingAction("Cut Selection") {

    init {
        putValue(SHORT_DESCRIPTION, "Delete and copy selected components")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK))

    }

    override fun actionPerformed(e: ActionEvent?) {
        val copy = CopySelectionAction(mainController)
        copy.actionPerformed(e)
        val delete = DeleteSelectionAction(mainController.getControllerFunc)
        delete.actionPerformed(e)
    }

    override fun chemDataChanged(activeSession: EditorSession, currentBond: ChemBond?, currentAtom: ChemAtom?) {
        isEnabled = activeSession.editorData.selectionManager.hasBatchSelection()
    }
}

class DeleteSelectionAction(val getController: () -> EditorStateController?) : MolGLideSwingAction("Delete Selection",
) {
    init {
        putValue(SHORT_DESCRIPTION, "Delete the selected components")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0))

    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.deleteSelectedComponents()
    }

    override fun chemDataChanged(activeSession: EditorSession, currentBond: ChemBond?, currentAtom: ChemAtom?) {
        isEnabled = activeSession.editorData.selectionManager.hasBatchSelection()
    }
}


class IgnoreErrorAction(val getController: () -> EditorStateController?
) : MolGLideSwingAtomAction("Ignore Valency Errors") {
    init {
        putValue(SHORT_DESCRIPTION, "Enable/Disable valency checking for this atom")

    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.ignoreErrors()
    }

}


class VisitWebsite(): MolGLideSwingAction("Visit Website") {
    init {
        putValue(SHORT_DESCRIPTION, "Visit the MolGLide website")
    }
    override fun actionPerformed(e: ActionEvent?) {
        Desktop.getDesktop().browse(URI(MolGLideUtils.WEBSITE))
    }
}

class VisitRepoAction(): MolGLideSwingAction("Visit Repository") {
    init {
        putValue(SHORT_DESCRIPTION, "Visit GitHub Repository")
    }
    override fun actionPerformed(e: ActionEvent?) {
        Desktop.getDesktop().browse(URI(MolGLideUtils.REPO))
    }
}

class VisitBugTrackerAction(): MolGLideSwingAction("Report Bugs") {
    init {
        putValue(SHORT_DESCRIPTION, "Visit the MolGLide issue tracker")
    }
    override fun actionPerformed(e: ActionEvent?) {
        Desktop.getDesktop().browse(URI(MolGLideUtils.BUG_TRACKER))
    }
}

class ShowAboutMenuAction(val mainFrame: MolGlideFrame) : MolGLideSwingAction("About MolGLide") {
    init {
        putValue(SHORT_DESCRIPTION, "About MolGLide")
    }
    override fun actionPerformed(e: ActionEvent?) {
        val dialogue = AboutDialogue(mainFrame)
        dialogue.isVisible = true
    }
}

class CDKCopyCanonicalSmilesAction (val mainController: MainController) : MolGLideSwingAction("Copy canonical SMILES") {

    init {
        putValue(SHORT_DESCRIPTION, "Copy the canonical SMILES of the selected molecule to the clipboard")
    }

    override fun chemDataChanged(activeSession: EditorSession, currentBond: ChemBond?, currentAtom: ChemAtom?
    ) {
        isEnabled = activeSession.editorData.selectionManager.getMolecule() != null
    }

    override fun actionPerformed(e: ActionEvent?) {
        mainController.copyAsSmiles()
    }
}

class CDKCleanupStructure (val getController: () -> EditorStateController?) : MolGLideSwingAction("Clean Structure") {

    init {
        putValue(SHORT_DESCRIPTION, "Invoke CDK to clean the structure of the molecule")
    }

    override fun chemDataChanged(activeSession: EditorSession, currentBond: ChemBond?, currentAtom: ChemAtom?
    ) {
        isEnabled = activeSession.editorData.selectionManager.getMolecule() != null
    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.cleanUpSelectedMolecule()
    }
}

class CDKCopyInChi (val mainController: MainController): MolGLideSwingAction("Copy InChi") {

    init {
        putValue(SHORT_DESCRIPTION, "Invoke CDK to generate the InChi for this molecule")
    }

    override fun actionPerformed(e: ActionEvent?) {
        mainController.copyInChi()
    }

    override fun chemDataChanged(activeSession: EditorSession, currentBond: ChemBond?, currentAtom: ChemAtom?
    ) {
        isEnabled = activeSession.editorData.selectionManager.getMolecule() != null
    }

}


class SetSingleElectronTransfer(val getController: () -> EditorStateController?) : MolGLideSwingAction("Single Barbed Arrow") {

    init {
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_1, 0))
    }

    override fun chemDataChanged(activeSession: EditorSession, currentBond: ChemBond?, currentAtom: ChemAtom?) {
        val selectionInfo = activeSession.editorData.selectionManager.primarySelection
        val arrow = selectionInfo?.selectable
        val objectID = selectionInfo?.objectAnchorID
        if (arrow !is ChemArrow || objectID == null) {
            isEnabled = false
            return
        }
        isEnabled = objectID == 0 || objectID == 1
        putValue(SELECTED_KEY, arrow.getArrowType(objectID) == ChemArrow.ArrowHead.SINGLE_BARBED)


    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.updateSelectedArrowHead(ChemArrow.ArrowHead.SINGLE_BARBED)
    }
}

class SetDoubleElectronTransfer(val getController: () -> EditorStateController?) : MolGLideSwingAction("Double Barbed Arrow") {

    init {
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_2, 0))
    }

    override fun chemDataChanged(activeSession: EditorSession, currentBond: ChemBond?, currentAtom: ChemAtom?) {
        val selectionInfo = activeSession.editorData.selectionManager.primarySelection
        val arrow = selectionInfo?.selectable
        val objectID = selectionInfo?.objectAnchorID
        if (arrow !is ChemArrow || objectID == null) {
            isEnabled = false
            return
        }
        isEnabled = objectID == 0 || objectID == 1
        putValue(SELECTED_KEY, arrow.getArrowType(objectID) == ChemArrow.ArrowHead.DOUBLE_BARBED)
    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.updateSelectedArrowHead(ChemArrow.ArrowHead.DOUBLE_BARBED)
    }
}

class SetNoElectronTransfer(val getController: () -> EditorStateController?) : MolGLideSwingAction("No Arrow Head") {

    init {
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_3, 0))
    }

    override fun chemDataChanged(activeSession: EditorSession, currentBond: ChemBond?, currentAtom: ChemAtom?) {
        val selectionInfo = activeSession.editorData.selectionManager.primarySelection
        val arrow = selectionInfo?.selectable
        val objectID = selectionInfo?.objectAnchorID
        if (arrow !is ChemArrow || objectID == null) {
            isEnabled = false
            return
        }
        isEnabled = objectID == 0 || objectID == 1
        putValue(SELECTED_KEY, arrow.getArrowType(objectID) == ChemArrow.ArrowHead.NONE)
    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.updateSelectedArrowHead(ChemArrow.ArrowHead.NONE)
    }
}


