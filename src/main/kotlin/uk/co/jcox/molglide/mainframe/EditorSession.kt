package uk.co.jcox.molglide.mainframe

import uk.co.jcox.molglide.control.EditorStateController
import uk.co.jcox.molglide.control.EditorStateData
import uk.co.jcox.molglide.ui.EditorPanel

data class EditorSession(
    val id: String,
    val editorData: EditorStateData,
    val editorController: EditorStateController,
    val editorPanel: EditorPanel,
)
