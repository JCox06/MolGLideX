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
        if (activeTab == null) {

        }
    }

    fun handleGlobalRedo() {
        activeTab?.actionManager?.restoreLastAction()
    }

    companion object {
        private const val EDITOR_PREFIX: String = "internal_editor_"
    }
}