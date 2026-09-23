package uk.co.jcox.molglide

import kotlin.math.pow

class MainData : IMainAppData{


    var editToolMode = EditMode.INSERT_CARBON

    //A list of editor sessions
    val sessions = mutableMapOf<String, EditorSession>()

    //I am making the list of dockable panels separate to the editor sessions
    //since at some point, it might be beneficial to have a global list of dockable panels
    //that may or may not be editor sessions
    val modernDockingManaged = mutableMapOf<String, DockingPanel>()

    var activeSession: EditorSession? = null

    override fun getEditMode(): EditMode {
        return editToolMode
    }

    override fun getSelectedFormula(): String {
        return activeSession?.editorData?.uiDataBuilder?.getSelectedFormula() ?: return ""
    }

    override fun getSelectedWeight(): Double {
        return activeSession?.editorData?.uiDataBuilder?.getSelectedWeight() ?: 0.0

    }

    override fun getSelectedHybrid(): String {
        return activeSession?.editorData?.uiDataBuilder?.getSelectedHybridisation() ?: return ""
    }

    override fun getMoleculeCount(): Int {
        return activeSession?.editorData?.getMolecules()?.size ?: 0
    }

    override fun getProcessingTime(): Double {
        val delta = activeSession?.editorController?.deltaTime ?: 0
        val divisor = 10.0.pow(6)
        return delta / divisor
    }
}