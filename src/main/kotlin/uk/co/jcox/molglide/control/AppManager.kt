package uk.co.jcox.molglide.control

import uk.co.jcox.molglide.EditMode

class AppManager {

    private var idIncrement = 0

    private val editorStateData: MutableMap<String, EditorStateData> = mutableMapOf()

    var editMode: EditMode = EditMode.INSERT_CARBON

    var activeTab: EditorStateController? = null

    fun isRegistered(id: String): Boolean {
        return editorStateData.containsKey(id)
    }

    fun getDataForState(id: String) : EditorStateData {
        return editorStateData[id] ?: throw NoSuchElementException("Project with ${id} not found in data")
    }

    fun createEmpty() : String {
        val newID = "${EDITOR_PREFIX}${idIncrement}"
        val id = idIncrement
        idIncrement++
        if (isRegistered(newID)) {
            return createEmpty()
        }
        val state = EditorStateData(id)
        editorStateData[newID] = state
        return newID
    }


    fun handleGlobalUndo() {
        activeTab?.actionManager?.undoLastAction()
    }

    fun handleGlobalRedo() {
        activeTab?.actionManager?.restoreLastAction()
    }

    fun canUndo(): Boolean {
        return activeTab?.actionManager?.canUndo() ?: return false
    }

    fun canRedo(): Boolean {
        return activeTab?.actionManager?.canRedo() ?: return false
    }

    companion object {
        private const val EDITOR_PREFIX: String = "internal_editor_"
    }
}