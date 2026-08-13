package uk.co.jcox.molglide.ui

import org.joda.time.DateTime
import uk.co.jcox.molglide.MolGLideUtils
import uk.co.jcox.molglide.control.AppManager
import uk.co.jcox.molglide.control.EditorStateController
import uk.co.jcox.molglide.control.SVGExporter
import java.awt.Desktop
import java.awt.event.ActionEvent
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.WindowEvent
import java.io.File
import javax.swing.AbstractAction
import javax.swing.KeyStroke

class UndoAction (val appManager: AppManager) : AbstractAction("Undo") {

    init {
        putValue(NAME, "Undo")
        putValue(SHORT_DESCRIPTION, "Undo the last operation")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK))
    }

    override fun actionPerformed(e: ActionEvent?) {
        appManager.handleGlobalUndo()
    }
}

class RedoAction (val appManager: AppManager) : AbstractAction("Redo") {

    init {
        putValue(NAME, "Redo")
        putValue(SHORT_DESCRIPTION, "Redo the last operation")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK))
    }

    override fun actionPerformed(e: ActionEvent?) {
        appManager.handleGlobalRedo()
    }

}

class QuitAction (val mainFrame: MolGlideFrame) : AbstractAction("Quit") {

    init {
        putValue(SHORT_DESCRIPTION, "Quits the Application")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK))
    }

    override fun actionPerformed(e: ActionEvent?) {
        mainFrame.dispatchEvent(WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING))
    }

}


class NewProjectAction (val mainFrame: MolGlideFrame,val appManager: AppManager) : AbstractAction("New MGF Project") {

    init {
        putValue(SHORT_DESCRIPTION, "Create a new MolGLideX project")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK))
    }

    override fun actionPerformed(e: ActionEvent?) {
        val newID = appManager.createEmpty()
        val data = appManager.getDataForState(newID)
        val dataController = EditorStateController(appManager, data)
        val editorPanel = EditorPanel(dataController)
        mainFrame.addDockingPanel(newID, "Editor${data.currentID}", editorPanel, true)    }

}


class QuickCaptureAction (val appManager: AppManager) : AbstractAction("Quick Capture") {

    init {
        putValue(SHORT_DESCRIPTION, "Captures, saves, and opens the current view as an SVG image.")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK))
    }

    override fun actionPerformed(e: ActionEvent?) {
        val currentPane = appManager.activePanel
        if (currentPane != null) {
            val exporter = SVGExporter()
            val userHome = File(System.getProperty("user.home"))
            val file = File(MolGLideUtils.getQuickCaptureDirectory(), DateTime.now().toString())
            exporter.quickExport(currentPane, file)
            Desktop.getDesktop().browse(file.toURI())
        }
    }
}

class EditLabelAction (val controller: EditorStateController) : AbstractAction("Edit Label") {
    init {
        putValue(SHORT_DESCRIPTION, "Edits the atom label")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0))
    }

    override fun actionPerformed(e: ActionEvent?) {
        println("Edited the label")
    }
}

class DeleteAtomAction (val controller: EditorStateController) : AbstractAction("Delete Atom") {
    init {
        putValue(SHORT_DESCRIPTION, "Deletes the atom")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0))
    }

    override fun actionPerformed(e: ActionEvent?) {
        println("Deleted the atom")
    }
}

class ToggleAtomVisibilityMenuAction (val controller: EditorStateController) : AbstractAction("Atom Visible") {
    init {
        putValue(SHORT_DESCRIPTION, "Select whether this atom should be visible")
        putValue(SELECTED_KEY, true)
    }

    override fun actionPerformed(e: ActionEvent?) {
        println("Changed the atom visiblity")
    }
}

class FlipBondMenuAction (val controller: EditorStateController) : AbstractAction("Flip Bond") {
    init {
        putValue(SHORT_DESCRIPTION, "Toggles the side of the double bond")
    }

    override fun actionPerformed(e: ActionEvent?) {
        println("Changed side of the double bond")
    }
}

class SetPlainBondMenuAction (val controller: EditorStateController) : AbstractAction("Plain") {
    init {
        putValue(SHORT_DESCRIPTION, "Select single bond")
        putValue(SELECTED_KEY, true)
    }

    override fun actionPerformed(e: ActionEvent?) {
        println("Changed the atom bond")
    }
}

class SetWedgedBondMenuAction (val controller: EditorStateController) : AbstractAction("Wedged") {
    init {
        putValue(SHORT_DESCRIPTION, "Select wedged bond")
        putValue(SELECTED_KEY, true)
    }

    override fun actionPerformed(e: ActionEvent?) {
        println("Changed the atom bond")
    }
}

class SetDashedBondMenuAction (val controller: EditorStateController) : AbstractAction("Wedged") {
    init {
        putValue(SHORT_DESCRIPTION, "Select dashed bond")
        putValue(SELECTED_KEY, true)
    }

    override fun actionPerformed(e: ActionEvent?) {
        println("Changed the atom bond")
    }
}

class SetDoubleBondMenuAction (val controller: EditorStateController) : AbstractAction("Plain") {
    init {
        putValue(SHORT_DESCRIPTION, "Select double bond")
        putValue(SELECTED_KEY, true)
    }

    override fun actionPerformed(e: ActionEvent?) {
        println("Changed the atom bond")
    }
}

class SetAromaticDoubleBondMenuAction (val controller: EditorStateController) : AbstractAction("Aromatic") {
    init {
        putValue(SHORT_DESCRIPTION, "Select aromatic bond")
        putValue(SELECTED_KEY, true)
    }

    override fun actionPerformed(e: ActionEvent?) {
        println("Changed the atom bond")
    }
}

class SetCentreDoubleBondMenuAction (val controller: EditorStateController) : AbstractAction("Centre Bond") {
    init {
        putValue(SHORT_DESCRIPTION, "Select aromatic bond")
        putValue(SELECTED_KEY, true)
    }

    override fun actionPerformed(e: ActionEvent?) {
        println("Changed the atom bond")
    }
}

class SetTripleBondMenuAction (val controller: EditorStateController) : AbstractAction("Triple Bond") {
    init {
        putValue(SHORT_DESCRIPTION, "Select Triple bond")
        putValue(SELECTED_KEY, true)
    }

    override fun actionPerformed(e: ActionEvent?) {
        println("Changed the atom bond")
    }
}

class DeleteBondMenuAction (val controller: EditorStateController) : AbstractAction("Delete Bond") {
    init {
        putValue(SHORT_DESCRIPTION, "Select Triple bond")
        putValue(SELECTED_KEY, true)
    }

    override fun actionPerformed(e: ActionEvent?) {
        println("Changed the atom bond")
    }
}

