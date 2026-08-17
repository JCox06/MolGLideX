package uk.co.jcox.molglide.ui

import com.github.jsonldjava.shaded.com.google.common.math.IntMath.pow
import com.sun.org.apache.xpath.internal.operations.Bool
import org.joml.Vector2d
import uk.co.jcox.molglide.MolGLideUtils
import uk.co.jcox.molglide.StereoChem
import uk.co.jcox.molglide.control.ChemMolecule
import uk.co.jcox.molglide.control.EditorStateController
import uk.co.jcox.molglide.control.UIAtom
import uk.co.jcox.molglide.control.UIBond
import java.awt.BasicStroke
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.RenderingHints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.awt.font.TextAttribute
import java.text.AttributedString
import javax.swing.JCheckBoxMenuItem
import javax.swing.JMenu
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.SwingUtilities
import javax.swing.Timer
import javax.swing.event.PopupMenuEvent
import javax.swing.event.PopupMenuListener
import kotlin.collections.toIntArray
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.sqrt

/*
The editor panel is the class that actually draws the chemistry stuff

The Editor Panel is actually simple, it requests each time it wants to paint, a set of lines to draw
(These make up the bonds - For instance, 1 line for a single bond, 2 lines for a double, a lot of lines for a dashed stereochem bond)

The Editor panel also gets a list of UIAtoms that it should paint each time

For wedged bonds, since these are not lines, or atoms, a completely new primitive is called for this - A triangle!

If the editor panel needs to find more information about a specific atom or bond, it can retrieve a UIBondContext
or UIAtomContext, or directly query if an entity is selected from the controller
 */
class EditorPanel(val dataController: EditorStateController) : JPanel() {

    private var cameraX: Double = 0.0
    private var cameraY: Double = 0.0
    private var cameraZoom: Float = 1.0f
        set(value) {field = max(1.0f, value)}
    private var lastMousePos: Point = Point()

    private var pauseEvents = false

    private var transientSelectionBoxStart = Point()
    private var transientSelectionBoxAdvance = Point()

    init {
        val timer = Timer(16) {
            repaint()
            val world = screenToWorld(lastMousePos)
            dataController.update(world.x, world.y)
        }
        timer.start()

        val mouseListener = MouseEvents()
        addMouseListener(mouseListener)
        addMouseMotionListener(mouseListener)
        addMouseWheelListener(mouseListener)
    }



    private fun buildBatchContextMenu(): JPopupMenu {
        val menu = JPopupMenu()
        menu.add(CopySelection(dataController, this))
        menu.add(CutSelection(dataController, this))
        menu.add(DeleteSelection(dataController))
        return menu
    }


    private fun buildBondContextMenu(bondContext: UIBond) : JPopupMenu {
        val menu = JPopupMenu()

        val menuSingle = JMenu("Single")
        menuSingle.add(JCheckBoxMenuItem(SetPlainBondMenuAction(dataController, bondContext.order == 1 && bondContext.stereo == StereoChem.NORMAL)))
        menuSingle.add(JCheckBoxMenuItem(SetWedgedBondMenuAction(dataController, bondContext.stereo == StereoChem.WEDGED)))
        menuSingle.add(JCheckBoxMenuItem(SetDashedBondMenuAction(dataController, bondContext.stereo == StereoChem.DASHED)))
        menuSingle.add(FlipStereoChemMenuAction(dataController))
        menu.add(menuSingle)

        val menuDouble = JMenu("Double")
        val isDouble = bondContext.order == 2
        menuDouble.add(JCheckBoxMenuItem(SetDoubleBondMenuAction(dataController, isDouble)))
        menuDouble.add(JCheckBoxMenuItem(SetAromaticDoubleBondMenuAction(dataController, bondContext.isAromatic)))
        val flipBondAction = FlipBondMenuAction(dataController)
        menuDouble.add(flipBondAction)
        menu.add(menuDouble)

        menu.add(JCheckBoxMenuItem(SetTripleBondMenuAction(dataController, bondContext.order == 3)))
        menu.add(DeleteBondMenuAction(dataController))

        menu.addSeparator()

        menu.add(buildCommonCDKMenu())

        applyMenuEvent(menu)

        return menu
    }

    private fun buildAtomContextMenu(atomContext: UIAtom) : JPopupMenu {
        val menu = JPopupMenu()

        val editLabelAction = EditLabelMenuAction(this,dataController)
        val deleteAtomMenuAction = DeleteAtomMenuAction(dataController)
        val toggleAtomVisibilityMenuActionAction = ToggleAtomVisibilityMenuAction(dataController, atomContext.visible)

        menu.add(editLabelAction)
        menu.add(deleteAtomMenuAction)
        menu.add(JCheckBoxMenuItem(toggleAtomVisibilityMenuActionAction))
        menu.addSeparator()
        menu.add(buildCommonCDKMenu())

        applyMenuEvent(menu)
        return menu
    }

    private fun buildCommonCDKMenu() : JMenu {
        val menuCDK = JMenu("Power CDK")
        menuCDK.add("CDK SVG Exporter")
        menuCDK.add("Copy Generic SMILES")
        menuCDK.add("Copy Canonical SMILES")
        return menuCDK
    }

    private fun applyMenuEvent(menu: JPopupMenu) {
        menu.addPopupMenuListener(object: PopupMenuListener{
            override fun popupMenuWillBecomeVisible(e: PopupMenuEvent?) {
                pauseEvents = true
            }

            override fun popupMenuWillBecomeInvisible(e: PopupMenuEvent?) {
                pauseEvents = false
            }

            override fun popupMenuCanceled(e: PopupMenuEvent?) {
                pauseEvents = false
            }

        })
    }


    /**
     * Paints the editor to a supplied Graphics2D object
     * @param g2d the Graphics2D object to paint to
     * @param drawSelectionMarkers Tells the painter to not draw square selection/anchor markers over atoms that
     * are selected
     * @param onlyDrawSelected By default, this is false. When set to true, the UIBuilder is called again, but this time
     * the UIBuilder will only build UI elements that are currently selected
     */
    fun paintEditor(g2d: Graphics2D, drawSelectionMarkers: Boolean = true, onlyDrawSelected: Boolean = false) {
        if (onlyDrawSelected) dataController.uiBuilder.rebuild(true)
        preparePainter(g2d)

        if (drawSelectionMarkers) {
            paintGenericSelectionBox(g2d)
            paintBondSelection(g2d)
        }
        //For the case of atoms, unfortunately, the selection marker
        //is coupled with the atom itself, so this has to be checked individually
        paintAtoms(g2d, drawSelectionMarkers)
        paintLines(g2d)
        paintTriangles(g2d)
    }


    /**
     * Should only be called by Java Swing
     * For external painting use EditorPanel#paintEditor
     */
    protected override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)

        val g2d = g as? Graphics2D ?: return

        paintEditor(g2d, true, false)
    }


    private fun preparePainter(g2d: Graphics2D) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g2d.font = Font("Liberation Serif", Font.PLAIN, -5)
        g2d.font = g2d.font.deriveFont(Font.PLAIN, UNMODDED_TEXT_SIZE * cameraZoom)

        g2d.translate(cameraX, cameraY)
    }


    /**
     * This is not for painting the selection markers as seen by the selection boxes
     * that show up on atoms and bonds when the mouse is near
     *
     * This is for painting the wide selection box that appears when using the selection box tool
     *
     * This is a special UI element, because it is drawn, regardless if the controller
     * has validated it.
     * -> As in, this is the only UI component that is painted from data that does not come from the
     * UIBuilder in the controller
     */
    private fun paintGenericSelectionBox(g2d: Graphics2D) {
        var startX = cameraZoom * transientSelectionBoxStart.x
        var startY = cameraZoom * transientSelectionBoxStart.y
        var advX = cameraZoom * transientSelectionBoxAdvance.x
        var advY = cameraZoom * transientSelectionBoxAdvance.y

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

    private fun paintAtoms(g2d: Graphics2D, paintSelection: Boolean) {
        val atomsToPaint = dataController.uiBuilder.getUIAtoms()
        atomsToPaint.forEach { ui ->
            val x = ui.posX * cameraZoom
            val y = ui.posY * cameraZoom

            val metrics = getMasterMetrics(g2d, ui, x, y)

            if (paintSelection) paintAtomSelection(g2d, ui, metrics)

            if (ui.visible) {
                paintMasterAtom(g2d, ui, metrics)
                if (ui.trailGroup != "") {
                    paintTrailGroup(g2d, ui, x, y, metrics)
                }
            }

        }
    }


    private fun getMasterMetrics(g2d: Graphics2D, uiAtom: UIAtom, x: Double, y: Double): MasterAtomMetric {
        val textWidth = g2d.fontMetrics.stringWidth(uiAtom.element)
        val textHeight = g2d.fontMetrics.ascent - g2d.fontMetrics.descent

        val centreTextWidth = x - textWidth / 2
        val centreBoxWidth = x - textWidth
        val centreTextHeight = y + textHeight / 2
        val centreBoxHeight = y - g2d.fontMetrics.height / 2
        return MasterAtomMetric(centreTextWidth, centreTextHeight, centreBoxWidth, centreBoxHeight, textWidth, textHeight)
    }

    private fun paintAtomSelection(g2d: Graphics2D, uiAtom: UIAtom, m: MasterAtomMetric) {
        if (uiAtom.selected) {
            val oldColour = g2d.color
            val newColour = MolGLideUtils.getAccentColour()
            g2d.color = newColour
            g2d.fillRoundRect((m.centreBoxWidth).toInt(),
                (m.centreBoxHeight).toInt(),
                (m.textWidth * 2),
                (m.textWidth * 2), m.textWidth, m.textWidth)
            g2d.color = oldColour
        }
    }

    private fun paintMasterAtom(g2d: Graphics2D, uiAtom: UIAtom,  m: MasterAtomMetric) {
        g2d.drawString(uiAtom.element, m.centreTextWidth.toInt(), m.centreTextHeight.toInt())
    }

    private fun paintTrailGroup(g2d: Graphics2D, uiAtom: UIAtom, x: Double, y: Double, m: MasterAtomMetric) {

        val s = getStartingPos(g2d,x, y, m, uiAtom)
        val startX = s.x
        val startY = s.y

        val attString = AttributedString(uiAtom.trailGroup)
        attString.addAttribute(TextAttribute.FAMILY, g2d.font.family)
        attString.addAttribute(TextAttribute.SIZE, g2d.font.size)

        val range = getSubscriptRange(uiAtom.trailGroup)
        range.forEach {
            attString.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUB,  it, it + 1)
        }
        g2d.drawString(attString.iterator, startX.toFloat(), startY.toFloat())
    }

    private fun getStartingPos(g2d: Graphics2D, x: Double, y: Double, m: MasterAtomMetric, uiAtom: UIAtom) : Vector2d {
        val startX = x + m.textWidth / 2
        val startY = y + m.textHeight / 2

        if (uiAtom.trailGroupPos == ChemMolecule.TrailingGroupPosition.RIGHT) {
            return Vector2d(startX, startY)
        }
        if (uiAtom.trailGroupPos == ChemMolecule.TrailingGroupPosition.LEFT) {
            val trail = uiAtom.trailGroup
            val textWidth = g2d.fontMetrics.stringWidth(trail)
            return Vector2d(startX - textWidth - m.textWidth, startY)
        }


        return Vector2d(startX, startY)
    }

    private fun getSubscriptRange(trailGroup: String) : List<Int> {
        val list = ArrayList<Int>()
        trailGroup.forEachIndexed { index, ch ->
            if (ch.isDigit()) {
                list.add(index)
            }
        }
        return list
    }

    private fun paintLines(g2d: Graphics2D) {
        g2d.stroke = BasicStroke(LINE_STROKE * cameraZoom, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        val bondsToPaint = dataController.uiBuilder.getUILines()
        bondsToPaint.forEach { bondUI ->
            val startX = bondUI.startX * cameraZoom
            val startY = bondUI.startY * cameraZoom
            val endX = bondUI.endX * cameraZoom
            val endY = bondUI.endY * cameraZoom

            g2d.drawLine(startX.toInt(), startY.toInt(), endX.toInt(), endY.toInt())
        }
    }


    private fun paintTriangles(g2d: Graphics2D) {
        val triangles = dataController.uiBuilder.getUITriangles()
        triangles.forEach { t ->
            val x = listOf<Int>((t.v1.x * cameraZoom).toInt(), (t.v2.x * cameraZoom).toInt(),
                (t.v3.x * cameraZoom).toInt()
            ).toIntArray()
            val y = listOf<Int>((t.v1.y * cameraZoom).toInt(), (t.v2.y * cameraZoom).toInt(), (t.v3.y * cameraZoom).toInt()).toIntArray()

            g2d.fillPolygon(x, y, 3)
        }
    }

    private fun paintBondSelection(g2d: Graphics2D) {
//        val bondContext = dataController.uiBuilder.getSelectedBond() ?: return
//        val bondPos = bondContext.midPoint
//        val width = BOND_MARKER * cameraZoom
//        val height = BOND_MARKER * cameraZoom
//        val start = getDiscreteSelectionBoxStart(bondPos, width, height)
//
//        val oldColour = g2d.color
//        g2d.color = MolGLideUtils.getAccentColour()
//
//        g2d.fillRect(
//            start.x.toInt(), start.y.toInt(), width.toInt(), height.toInt()
//                    )
//        g2d.color = oldColour


        val bondContexts = dataController.uiBuilder.getUIBonds()
        val width = BOND_MARKER * cameraZoom
        val height = BOND_MARKER * cameraZoom
        val oldColour = g2d.color
        g2d.color = MolGLideUtils.getAccentColour()
        bondContexts.forEach { bondContext ->
            if (bondContext.isSelected) {
                val start = getDiscreteSelectionBoxStart(bondContext.midPoint, width, height)
                g2d.fillRect(start.x.toInt(), start.y.toInt(), width.toInt(), height.toInt())
            }
        }
        g2d.color = oldColour
    }

    private fun getDiscreteSelectionBoxStart(bondPos: Vector2d, width: Float, height: Float): Vector2d {
        val startX = bondPos.x * cameraZoom
        val startY = bondPos.y * cameraZoom
        return Vector2d(startX - width / 2, startY - height / 2)
    }

    private fun screenToWorld(screen: Point) : Point {
        var x: Double = screen.x.toDouble()
        var y: Double = screen.y.toDouble()
        x -= cameraX
        y -= cameraY
        x /= cameraZoom
        y /= cameraZoom
        return Point(x.toInt(), y.toInt())
    }

    inner class MouseEvents: MouseAdapter() {

        private var offsetX: Int = 0
        private var offsetY: Int = 0

        override fun mouseClicked(e: MouseEvent?) {

        }

        override fun mousePressed(e: MouseEvent?) {
            if (e == null || pauseEvents) {
                return
            }

//            if (dataController.canDrawSelectOnClick() && SwingUtilities.isLeftMouseButton(e)) {
//                val point = screenToWorld(e.point)

//            }

            val point = screenToWorld(e.point)
            if (SwingUtilities.isLeftMouseButton(e)) {
                dataController.handleMouseClick(point.x, point.y)
            }
            transientSelectionBoxStart = point
            maybeShowPopup(e)
        }

        override fun mouseReleased(e: MouseEvent?) {
            if (e == null || pauseEvents) {
                return
            }
            val point = screenToWorld(e.point)
            dataController.handleMouseRelease(point.x, point.y)

            maybeShowPopup(e)

            transientSelectionBoxStart.x = 0
            transientSelectionBoxStart.y = 0
            transientSelectionBoxAdvance.x = 0
            transientSelectionBoxAdvance.y = 0
        }

        override fun mouseEntered(e: MouseEvent?) {

        }

        override fun mouseExited(e: MouseEvent?) {

        }

        override fun mouseWheelMoved(e: MouseWheelEvent?) {
            if (e == null || pauseEvents) {
                return
            }
            if(e.isControlDown) {
                cameraZoom -= e.wheelRotation * MOUSE_SENSE_ZOOM
            }
        }

        override fun mouseDragged(e: MouseEvent?) {
            if (e == null || pauseEvents) {
                return
            }

            val worldPoint = screenToWorld(e.point)
            updateMouse(e)
            val moveX = offsetX * MOUSE_SENSE
            val moveY = offsetY * MOUSE_SENSE
            dataController.handleMouseDrag(worldPoint.x, worldPoint.y, moveX, moveY)

            if (SwingUtilities.isMiddleMouseButton(e)) {
                cameraX += moveX
                cameraY += moveY
            }
            if (dataController.canDrawSelectOnClick() && SwingUtilities.isLeftMouseButton(e)) {
                transientSelectionBoxAdvance.x = worldPoint.x - transientSelectionBoxStart.x
                transientSelectionBoxAdvance.y = worldPoint.y - transientSelectionBoxStart.y
                dataController.updateAxisAlignedBoundingBox(transientSelectionBoxStart.x, transientSelectionBoxStart.y, worldPoint.x, worldPoint.y)
            }
        }

        override fun mouseMoved(e: MouseEvent?) {
            if (pauseEvents || e == null) {
                return
            }
            updateMouse(e)
            dataController.nowActive(this@EditorPanel)
        }

        private fun updateMouse(e: MouseEvent?) {
            if (e == null || pauseEvents) {
                return
            }
            val currentPos = e.point
            offsetX = currentPos.x - lastMousePos.x
            offsetY = currentPos.y - lastMousePos.y
            lastMousePos.x = currentPos.x
            lastMousePos.y = currentPos.y
            if (sqrt((pow(offsetX, 2) + pow(offsetY, 2)).toDouble()) >= SIG_MOUSE_DELTA) {
                dataController.handleSuddenMouseMove()
            }
        }

        private fun maybeShowPopup(e: MouseEvent) {
            if (e.isPopupTrigger && SwingUtilities.isRightMouseButton(e)) {
                //Check first to see if something is batch selected
                if (dataController.hasBatchSelection()) {
                    val batchContextMenu = buildBatchContextMenu()
                    batchContextMenu.show(e.component, e.x, e.y)
                    return
                }

                //If no batch selection was present, need to see what is primary selected
                val atomContext = dataController.uiBuilder.getSelectedAtom()
                if (atomContext != null) {
                    val atomContextMenu = buildAtomContextMenu(atomContext)
                    atomContextMenu.show(e.component, e.x, e.y)
                    return
                }

                val bondContext = dataController.uiBuilder.getSelectedBond()
                if (bondContext != null) {
                    val bondContextMenu = buildBondContextMenu(bondContext)
                    bondContextMenu.show(e.component, e.x, e.y)
                    return
                }
            }
        }
    }
    companion object {
        private const val MOUSE_SENSE = 2.0
        private const val MOUSE_SENSE_ZOOM = 0.5f
        private const val UNMODDED_TEXT_SIZE = 32.0f
        private const val SIG_MOUSE_DELTA = 2.0f
        private const val LINE_STROKE = 3.0f
        private const val BOND_MARKER = UNMODDED_TEXT_SIZE * 0.5f
    }
}