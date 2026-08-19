package uk.co.jcox.molglide

import uk.co.jcox.molglide.editor.control.EditorStateController
import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.ui.EditorPanel
import java.io.File

data class EditorSession(
    val id: String,
    val editorData: EditorStateData,
    val editorController: EditorStateController,
    val editorPanel: EditorPanel,
    var saveFile: File? = null
)
