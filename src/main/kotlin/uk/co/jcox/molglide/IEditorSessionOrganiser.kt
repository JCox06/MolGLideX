package uk.co.jcox.molglide

/**
 * Allows communication between Editor controllers (mini controllers) of the Editor panel
 * and the main application controller of the Main JFrame
 *
 * Specifically mini controllers can tell the main controller when something has happened
 * (document becomes dirty, etc)
 *
 * Or the mini controllers can request data from the main controller such as various actions a controller may
 * wish to display in its panel
 */
interface IEditorSessionOrganiser {

    fun onDocumentDirty(sessionID: String)
    fun getActionRegistry(): SwingActionRegistry


}