package uk.co.jcox.molglide

import uk.co.jcox.molglide.editor.control.EditorStateController
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.net.URI
import javax.swing.AbstractAction
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.KeyStroke

class UndoAction (val mainController: MainController) : AbstractAction("Undo") {

    init {
        putValue(NAME, "Undo")
        putValue(SHORT_DESCRIPTION, "Undo the last operation")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK))

        isEnabled = false
    }

    override fun actionPerformed(e: ActionEvent?) {
        mainController.handleGlobalUndo()
    }
}

class RedoAction (val mainController: MainController) : AbstractAction("Redo") {

    init {
        putValue(NAME, "Redo")
        putValue(SHORT_DESCRIPTION, "Redo the last operation")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK))

        isEnabled = false
    }

    override fun actionPerformed(e: ActionEvent?) {
        mainController.handleGlobalRedo()
    }

}

class QuitAction (val mainController: MainController) : AbstractAction("Quit") {

    init {
        putValue(SHORT_DESCRIPTION, "Quits the Application")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK))
    }

    override fun actionPerformed(e: ActionEvent?) {
        mainController.shutdown()
    }

}


class NewProjectAction (val mainController: MainController) : AbstractAction("New MGF Project") {

    init {
        putValue(SHORT_DESCRIPTION, "Create a new MolGLideX project")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK))
    }

    override fun actionPerformed(e: ActionEvent?) {
        mainController.newProject()
    }

}


abstract class AbstractEditorControllerAction(name: String, protected val getController: () -> EditorStateController?) : AbstractAction(name)



class EditLabelMenuAction (val panel: JFrame, getController: () -> EditorStateController?)
    : AbstractEditorControllerAction("Edit Label", getController) {
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

class DeleteAtomMenuAction (getController: () -> EditorStateController?)
    : AbstractEditorControllerAction("Delete Atom", getController) {
    init {
        putValue(SHORT_DESCRIPTION, "Deletes the atom")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0))
    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.deleteSelectedAtom()
    }
}

class ToggleAtomVisibilityMenuAction (getController: () -> EditorStateController?
) : AbstractEditorControllerAction("Atom Visible", getController) {
    init {
        putValue(SHORT_DESCRIPTION, "Select whether this atom should be visible")
//        putValue(SELECTED_KEY, isVisible)
    }

    fun setSelected(isSelected: Boolean) {
        putValue(SELECTED_KEY, isSelected)
    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.toggleSelectedAtomVisibility()
    }
}

class FlipBondMenuAction (getController: () -> EditorStateController?
) : AbstractEditorControllerAction("Flip Double Bond", getController) {
    init {
        putValue(SHORT_DESCRIPTION, "Toggles the side of the double bond")
    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.flipSelectedBond()
    }
}

class SetPlainBondMenuAction (getController: () -> EditorStateController?)
    : AbstractEditorControllerAction("Plain Bond", getController
) {
    init {
        putValue(SHORT_DESCRIPTION, "Select single bond")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_1, 0))
    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.updateSingleSelectedBond(StereoChem.NORMAL)
    }

    fun setSelected(isPlainBond: Boolean) {
        putValue(SELECTED_KEY, isPlainBond)
    }
}

class SetWedgedBondMenuAction (getController: () -> EditorStateController?) : AbstractEditorControllerAction("Wedged Bond", getController) {
    init {
        putValue(SHORT_DESCRIPTION, "Select wedged bond")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, 0))


//        putValue(SELECTED_KEY, isWedged)
    }

    fun setSelected(isPlainBond: Boolean) {
        putValue(SELECTED_KEY, isPlainBond)
    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.updateSingleSelectedBond(StereoChem.WEDGED)
    }
}

class SetDashedBondMenuAction (getController: () -> EditorStateController?)
    : AbstractEditorControllerAction("Hashed Bond", getController) {
    init {
        putValue(SHORT_DESCRIPTION, "Select dashed bond")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H, 0))

//        putValue(SELECTED_KEY, isDashed)
    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.updateSingleSelectedBond(StereoChem.DASHED)
    }

    fun setSelected(isPlainBond: Boolean) {
        putValue(SELECTED_KEY, isPlainBond)
    }
}

class FlipStereoChemMenuAction (getController: () -> EditorStateController?)
    : AbstractEditorControllerAction("Flip Stereo Chem", getController) {
    init {
        putValue(SHORT_DESCRIPTION, "Flip the direction of stereo chem")
    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.invertStereoChemSelectedBond()
    }
}


class SetDoubleBondMenuAction (getController: () -> EditorStateController?) : AbstractEditorControllerAction("Plain Double", getController) {
    init {
        putValue(SHORT_DESCRIPTION, "Select double bond")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_2, 0))

    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.updateDoubleSelectedBond()
    }
    fun setSelected(isPlainBond: Boolean) {
        putValue(SELECTED_KEY, isPlainBond)
    }
}

class SetAromaticDoubleBondMenuAction (getController: () -> EditorStateController?) : AbstractEditorControllerAction("Aromatic Bond",
    getController
) {
    init {
        putValue(SHORT_DESCRIPTION, "Select aromatic bond")
    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.updateAromaticSelectedBond()
    }

    fun setSelected(isPlainBond: Boolean) {
        putValue(SELECTED_KEY, isPlainBond)
    }
}

class SetTripleBondMenuAction (getController: () -> EditorStateController?) : AbstractEditorControllerAction("Triple Bond",
    getController
) {
    init {
        putValue(SHORT_DESCRIPTION, "Select Triple bond")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_3, 0))

    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.setTripleSelectedBond()
    }

    fun setSelected(isPlainBond: Boolean) {
        putValue(SELECTED_KEY, isPlainBond)
    }
}

class DeleteBondMenuAction (getController: () -> EditorStateController?) : AbstractEditorControllerAction("Delete Bond",
    getController
) {
    init {
        putValue(SHORT_DESCRIPTION, "Select Triple bond")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0))

    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.deleteSelectedBond()
    }
}


class SaveFileAction (val mainController: MainController) : AbstractAction("Save file") {
    init {
        putValue(SHORT_DESCRIPTION, "Saves current progress as .mgx file")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK))

    }

    override fun actionPerformed(e: ActionEvent?) {
        mainController.saveActiveProject()
    }

}


class SaveAsFileAction (val mainController: MainController, val mainFrame: MolGlideFrame) : AbstractAction("Save as") {
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


class LoadFileAction (val mainController: MainController, val mainFrame: MolGlideFrame) : AbstractAction("Load file") {

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


class CopySelectionAction(val mainController: MainController) : AbstractAction("Copy Selection") {

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
}

class PasteSelectionAction(val mainController: MainController) : AbstractAction("Paste Selection") {

    private val clipboard = Toolkit.getDefaultToolkit().systemClipboard

    init {
        putValue(SHORT_DESCRIPTION, "Paste selected components from the clipboard")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK))
    }

    override fun actionPerformed(e: ActionEvent?) {
        mainController.pasteSelectedMolecules()
    }
}

class CutSelectionAction(val mainController: MainController) : AbstractAction("Cut Selection") {

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
}

class DeleteSelectionAction(getController: () -> EditorStateController?) : AbstractEditorControllerAction("Delete Selection",
    getController
) {
    init {
        putValue(SHORT_DESCRIPTION, "Delete the selected components")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0))

    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.deleteSelectedComponents()
    }
}


class IgnoreErrorAction(getController: () -> EditorStateController?
) : AbstractEditorControllerAction("Ignore Valency Errors", getController) {
    init {
        putValue(SHORT_DESCRIPTION, "Enable/Disable valency checking for this atom")

    }

    override fun actionPerformed(e: ActionEvent?) {
        getController()?.ignoreErrors()
    }
    fun setSelected(isPlainBond: Boolean) {
        putValue(SELECTED_KEY, isPlainBond)
    }
}


class VisitWebsite(): AbstractAction("Visit Website") {
    init {
        putValue(SHORT_DESCRIPTION, "Visit the MolGLide website")
    }
    override fun actionPerformed(e: ActionEvent?) {
        Desktop.getDesktop().browse(URI(MolGLideUtils.WEBSITE))
    }
}

class VisitRepoAction(): AbstractAction("Visit Repository") {
    init {
        putValue(SHORT_DESCRIPTION, "Visit GitHub Repository")
    }
    override fun actionPerformed(e: ActionEvent?) {
        Desktop.getDesktop().browse(URI(MolGLideUtils.REPO))
    }
}

class VisitBugTrackerAction(): AbstractAction("Report Bugs") {
    init {
        putValue(SHORT_DESCRIPTION, "Visit the MolGLide issue tracker")
    }
    override fun actionPerformed(e: ActionEvent?) {
        Desktop.getDesktop().browse(URI(MolGLideUtils.BUG_TRACKER))
    }
}

class ShowAboutMenuAction(val mainFrame: MolGlideFrame) : AbstractAction("About MolGLide") {
    init {
        putValue(SHORT_DESCRIPTION, "About MolGLide")
    }
    override fun actionPerformed(e: ActionEvent?) {
        val dialogue = AboutDialogue(mainFrame)
        dialogue.isVisible = true
    }
}
