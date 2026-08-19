package uk.co.jcox.molglide

import io.github.andrewauclair.moderndocking.Dockable
import io.github.andrewauclair.moderndocking.app.Docking
import java.awt.BorderLayout
import javax.swing.JPanel

class DockingPanel(
    private val persistentID: String,
    var internalText: String) : Dockable, JPanel(BorderLayout()) {

    init {
        Docking.registerDockable(this)
    }

    override fun getPersistentID(): String? {
        return persistentID;
    }

    override fun getTabText(): String? {
        return internalText;
    }

}