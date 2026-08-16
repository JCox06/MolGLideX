package uk.co.jcox.molglide.control.tool

import uk.co.jcox.molglide.control.ActionManager
import uk.co.jcox.molglide.control.AppManager
import uk.co.jcox.molglide.control.EditorStateData
import uk.co.jcox.molglide.control.SelectionManager

class SelectTool(val appManager: AppManager, actionManager: ActionManager, selectionManager: SelectionManager, editorData: EditorStateData) : Tool(actionManager,
    selectionManager, editorData
) {
    override fun onClick(clickX: Int, clickY: Int) {

    }

    override fun onRelease(clickX: Int, clickY: Int) {

    }

    override fun onDragMouse(clickX: Int, clickY: Int) {

    }

    override fun onSuddenMove() {

    }
}