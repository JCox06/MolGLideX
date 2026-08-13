package uk.co.jcox.molglide.control

data class UIAtom (
    val element: String,
    val posX: Double,
    val posY: Double,
    val trailGroup: String,
    val trailGroupPos: ChemMolecule.TrailingGroupPosition,
    val visible: Boolean,
    val chemID: String,
)