package uk.co.jcox.molglide.ui

import uk.co.jcox.molglide.EditMode
import uk.co.jcox.molglide.control.AppManager
import java.awt.BorderLayout
import java.awt.GridLayout
import java.awt.Insets
import java.awt.Point
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.HierarchyEvent
import javax.swing.*
import javax.swing.plaf.basic.BasicToolBarUI
import kotlin.math.max


class Toolbox(private val appManager: AppManager) : JToolBar("Toolbox") {
    init {
        isFloatable = true
        isRollover = true

        val panel = JPanel(GridLayout(2, 2, 5, 5))

        setupToolbox(panel)

        addComponentListener(object: ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                val panelWidth = panel.width
                val minCellWidth = 80
                val cols = max(1, panelWidth / minCellWidth)

                val gridOptions: GridLayout = panel.layout as GridLayout
                gridOptions.columns = cols
                gridOptions.rows = 0
            }
        })

        addHierarchyListener { event ->
            if ((event.changeFlags and HierarchyEvent.PARENT_CHANGED.toLong())== 0L) {
                return@addHierarchyListener
            }
            if (! (ui as BasicToolBarUI).isFloating) {
                return@addHierarchyListener
            }
            val topLevel = SwingUtilities.windowForComponent(this)
            if (topLevel is JDialog) {
                topLevel.isResizable = true
            }
        }
        add(panel)
    }


    private fun setupToolbox(panel: JPanel) {
        val elementGroup = ButtonGroup()
        EditMode.entries.forEach { mode ->
            val button = JToggleButton()
            button.isSelected = mode == appManager.editMode
            if (mode.icon != null) {
                val icon = mode.icon.derive(16, 16)
                button.icon = icon
            } else {
                button.text = mode.symbol
            }
            button.preferredSize = button.minimumSize
            button.margin = Insets(0, 0, 0, 0)
            button.addActionListener {appManager.editMode = mode}
            elementGroup.add(button)
            panel.add(button)
        }
    }



}