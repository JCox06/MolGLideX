package uk.co.jcox.molglide.ui

import io.github.andrewauclair.moderndocking.Dockable
import io.github.andrewauclair.moderndocking.app.Docking
import io.github.andrewauclair.moderndocking.ui.DockingHeaderUI
import io.github.andrewauclair.moderndocking.ui.HeaderController
import io.github.andrewauclair.moderndocking.ui.HeaderModel
import java.awt.BorderLayout
import javax.swing.JPanel

class DockingPanel(private val persistentID: String, private val tabText: String) : Dockable, JPanel(BorderLayout()) {

    init {
        Docking.registerDockable(this)
    }

    override fun getPersistentID(): String? {
        return persistentID;
    }

    override fun getTabText(): String? {
        return tabText;
    }

}