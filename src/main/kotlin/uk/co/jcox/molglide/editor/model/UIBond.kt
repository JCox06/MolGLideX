package uk.co.jcox.molglide.editor.model

import org.joml.Vector2d
import uk.co.jcox.molglide.MolGLideUtils
import uk.co.jcox.molglide.StereoChem
import uk.co.jcox.molglide.editor.ui.EditorPanel.Companion.BOND_MARKER
import java.awt.Graphics2D

//INFORMATION
//Now that the batch selection tool has been added, it is now required that the selection manager
//supports selecting multiple atoms and bonds.
//
//This leads to a problem where instead of the editor panel asking the controller
//for the position of the currently selected bond or atom, the panel now needs to know right away
//what is selected and what is not.
//
//For the Atom, this is simple, you can use the UIAtom.
//For the Bond, this is more difficult, as one bond may draw two lines, or three lines,
//or one bond might be a triangle (wedge) or multiple lines (dash) as in the case for stereochemistry
//
//The UIBondContext class provides the information to the editor but not the drawing instructions, that is left
//to UITriangle and UILine.



class UIBond (
    val order: Int,
    val midPoint: Vector2d,
    val isAromatic: Boolean,
    val stereo: StereoChem,
    val bondLines: List<AbstractUIComponent>,
    isSelected: Boolean

) : AbstractUIComponent(isSelected) {

    override fun drawComponent(g2d: Graphics2D, cameraZoom: Double) {
        bondLines.forEach { it.drawComponent(g2d, cameraZoom) }
    }


    private fun getDiscreteSelectionBoxStart(camZoom: Double, width: Float, height: Float): Vector2d {
        val startX = midPoint.x * camZoom
        val startY = midPoint.y * camZoom
        return Vector2d(startX - width / 2, startY - height / 2)
    }

    override fun drawSelectionMarker(g2d: Graphics2D, cameraZoom: Double) {
        val width = BOND_MARKER * cameraZoom
        val height = BOND_MARKER * cameraZoom
        val oldColour = g2d.color
        g2d.color = MolGLideUtils.getAccentColour()
        if (selected) {
            val start = getDiscreteSelectionBoxStart(cameraZoom, width.toFloat(), height.toFloat())
            g2d.fillRect(start.x.toInt(), start.y.toInt(), width.toInt(), height.toInt())
        }
        g2d.color = oldColour
    }
}