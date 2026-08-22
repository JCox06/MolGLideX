package uk.co.jcox.molglide.editor.ui

import uk.co.jcox.molglide.MainController
import uk.co.jcox.molglide.MainController.Companion.TOGGLE_ATOM_VISIBILITY_ACTION
import uk.co.jcox.molglide.MolGLideUtils
import uk.co.jcox.molglide.SwingActionRegistry
import uk.co.jcox.molglide.editor.model.IDataModelUI
import java.awt.BasicStroke
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.MouseEvent
import javax.swing.JCheckBoxMenuItem
import javax.swing.JMenu
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.event.PopupMenuListener
import kotlin.math.absoluteValue


class EditorPanel(private val uiData: IDataModelUI) : JPanel() {


    var bondMenu: JPopupMenu? = null
    var atomMenu: JPopupMenu? = null
    var selectionMenu: JPopupMenu? = null
    var normalMenu: JPopupMenu? = null

    /**
     * Called by the controller 60 times a second
     */
    fun refreshEditor() {
        repaint()
    }

    fun buildContextMenus(actionRegistry: SwingActionRegistry, popupListener: PopupMenuListener) {
        fun buildCDKCommon(m: JPopupMenu) {
            val cdkMenu = JMenu("CDK Tools")
            cdkMenu.add(actionRegistry[MainController.CDK_COPY_CANONICAL_SMILES_ACTION])
            cdkMenu.add(actionRegistry[MainController.CDK_CLEANUP_STRUCTURE])
            cdkMenu.add(actionRegistry[MainController.CDK_INCHI])
            m.add(cdkMenu)
        }


        val bondContextMenu = JPopupMenu()

        val menuSingle = JMenu("Single")
        menuSingle.add(JCheckBoxMenuItem(actionRegistry[MainController.SET_SINGLE_BOND_ACTION]))
        menuSingle.add(JCheckBoxMenuItem(actionRegistry[MainController.SET_WEDGED_ACTION]))
        menuSingle.add(JCheckBoxMenuItem(actionRegistry[MainController.SET_HASHED_ACTION]))
        menuSingle.add(actionRegistry[MainController.FLIP_STEREO_CHEM_ACTION])
        bondContextMenu.add(menuSingle)

        val menuDouble = JMenu("Double")
        menuDouble.add(JCheckBoxMenuItem(actionRegistry[MainController.SET_DOUBLE_BOND_ACTION]))
        menuDouble.add(JCheckBoxMenuItem(actionRegistry[MainController.SET_AROMATIC_ACTION]))
        menuDouble.add(actionRegistry[MainController.FLIP_SELECTED_ACTION])
        bondContextMenu.add(menuDouble)

        bondContextMenu.add(JCheckBoxMenuItem(actionRegistry[MainController.SET_TRIPLE_BOND_ACTION]))
        bondContextMenu.add(actionRegistry[MainController.DELETE_BOND_ACTION])
        bondContextMenu.addSeparator()
        buildCDKCommon(bondContextMenu)

        bondContextMenu.addPopupMenuListener(popupListener)

        val selectionContextMenu = JPopupMenu()
        selectionContextMenu.add(actionRegistry[MainController.COPY_SELECTION_ACTION])
        selectionContextMenu.add(actionRegistry[MainController.PASTE_SELECTION_ACTION])
        selectionContextMenu.add(actionRegistry[MainController.CUT_SELECTION_ACTION])
        selectionContextMenu.add(actionRegistry[MainController.DELETE_SELECTION_ACTION])
        selectionContextMenu.addPopupMenuListener(popupListener)

        val genericContextMenu = JPopupMenu()
        genericContextMenu.add(actionRegistry[MainController.PASTE_SELECTION_ACTION])
        genericContextMenu.addPopupMenuListener(popupListener)

        val atomContextMenu = JPopupMenu()
        atomContextMenu.add(JCheckBoxMenuItem(actionRegistry[MainController.IGNORE_VALENCY_ERRORS_ACTION]))
        atomContextMenu.add(actionRegistry[MainController.EDIT_LABEL_ACTION])
        atomContextMenu.add(actionRegistry[MainController.DELETE_ATOM_MENU_ACTION])
        atomContextMenu.add(JCheckBoxMenuItem(actionRegistry[TOGGLE_ATOM_VISIBILITY_ACTION]))
        atomContextMenu.addSeparator()
        atomContextMenu.add(actionRegistry[MainController.DELETE_ATOM_MENU_ACTION])
        atomContextMenu.addPopupMenuListener(popupListener)
        atomContextMenu.addSeparator()
        buildCDKCommon(atomContextMenu)

        atomMenu = atomContextMenu
        bondMenu = bondContextMenu
        selectionMenu = selectionContextMenu
        normalMenu = genericContextMenu
    }


    fun paintEditor(g2d: Graphics2D, drawTransients: Boolean = true, onlyDrawSelected: Boolean = false) {
        preparePainter(g2d)

        if (drawTransients) {
            paintGenericSelectionBox(g2d)
        }
        g2d.stroke = BasicStroke(getLineStroke(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)

        uiData.getUIComponents().forEach { uiComp ->
            if (drawTransients) {
                uiComp.drawSelectionMarker(g2d, uiData.cameraZoom())
            }
            if (!uiComp.selected && onlyDrawSelected) {
                return@forEach
            }
            uiComp.drawComponent(g2d, uiData.cameraZoom())
        }
    }

    protected override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        val g2d = g as? Graphics2D ?: return
        paintEditor(g2d, true, false)
    }

    private fun preparePainter(g2d: Graphics2D) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
        g2d.font = Font("Liberation Serif", Font.PLAIN, -5)
        g2d.font = g2d.font.deriveFont(Font.PLAIN, getFontSize())

        g2d.translate(uiData.cameraX(), uiData.cameraY())
    }

    private fun paintGenericSelectionBox(g2d: Graphics2D) {
        var startX = uiData.cameraZoom() * uiData.getTransientSelectionStartX()
        var startY = uiData.cameraZoom() * uiData.getTransientSelectionStartY()
        var advX = uiData.cameraZoom() * uiData.getTransientSelectionAdvX()
        var advY = uiData.cameraZoom() * uiData.getTransientSelectionAdvY()
        //Java's painter ensures that width is positive, and height negative
        //So we need to convert that here if that is not the case
        if (advX < 0) {
            startX += advX
            advX = advX.absoluteValue
        }
        if (advY < 0) {
            startY += advY
            advY = advY.absoluteValue
        }

        val oldColour = g2d.color
        g2d.color = MolGLideUtils.getFocusColour()
        g2d.fillRect(startX.toInt(), startY.toInt(), advX.toInt(), advY.toInt())
        g2d.color = MolGLideUtils.getAccentColour()
        g2d.drawRect(startX.toInt(), startY.toInt(), advX.toInt(), advY.toInt())
        g2d.color = oldColour
    }


    private fun getFontSize() : Float {
        return UNMODDED_TEXT_SIZE * uiData.cameraZoom().toFloat()
    }

    private fun getLineStroke(): Float {
        return LINE_STROKE * uiData.cameraZoom().toFloat()
    }


    companion object {
        const val MOUSE_SENSE = 2.0
        const val MOUSE_SENSE_ZOOM = 0.5f
        const val UNMODDED_TEXT_SIZE = 32.0f
        const val SIG_MOUSE_DELTA = 2.0f
        const val LINE_STROKE = 3.0f
        const val BOND_MARKER = UNMODDED_TEXT_SIZE * 0.5f
    }
}