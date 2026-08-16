package uk.co.jcox.molglide.control

import org.joml.Vector2d
import uk.co.jcox.molglide.StereoChem

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



data class UIBond (
    val order: Int,
    val midPoint: Vector2d,
    val isAromatic: Boolean,
    val stereo: StereoChem,
    val isSelected: Boolean
    )