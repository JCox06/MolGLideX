package uk.co.jcox.molglide.control.tool

import uk.co.jcox.molglide.control.ActionManager
import uk.co.jcox.molglide.mainframe.MainController
import uk.co.jcox.molglide.control.EditorStateData
import uk.co.jcox.molglide.control.SelectionManager
import uk.co.jcox.molglide.mainframe.IMainAppData

class FormalChargeLonePairTool(val globalContext: IMainAppData, actionManager: ActionManager,
                               selectionManager: SelectionManager) : Tool(actionManager, selectionManager) {


    override fun onClick(clickX: Int, clickY: Int) {

    }

    override fun onRelease(clickX: Int, clickY: Int) {

    }

    override fun onDragMouse(clickX: Int, clickY: Int, dx: Double, dy: Double) {

    }

    override fun onSuddenMove() {

    }
}