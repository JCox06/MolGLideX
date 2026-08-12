package uk.co.jcox.molglide.ui

import uk.co.jcox.molglide.control.AppManager
import uk.co.jcox.molglide.control.EditorStateController
import java.awt.Desktop
import java.awt.event.ActionEvent
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.WindowEvent
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
        //Save the SVG file
        //todo later
    }
}


