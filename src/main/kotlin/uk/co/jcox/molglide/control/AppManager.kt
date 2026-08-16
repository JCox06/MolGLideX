package uk.co.jcox.molglide.control

import org.checkerframework.checker.units.qual.mol
import uk.co.jcox.molglide.EditMode
import uk.co.jcox.molglide.io.LevelLoader
import uk.co.jcox.molglide.ui.EditorPanel
import java.io.File
import javax.swing.JPanel

class AppManager {

    private var idIncrement = 0

    private val editorStateData: MutableMap<String, EditorStateData> = mutableMapOf()

    var editMode: EditMode = EditMode.INSERT_CARBON

    var activeTab: EditorStateController? = null
    var activePanel: EditorPanel? = null

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

    fun loadFile(file: File) : String {
        val newID = "${EDITOR_PREFIX}${idIncrement}"
        val id = idIncrement
        idIncrement++

        if (isRegistered(newID)) {
            return loadFile(file)
        }

        val levelLoader = LevelLoader()
        val state = levelLoader.loadLevel(file, id)
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

    fun performQuickCapture() {

    }

    companion object {
        private const val EDITOR_PREFIX: String = "internal_editor_"


        fun getDockingID(id: Int) : String {
            return "${EDITOR_PREFIX}${id}"
        }

    }
}