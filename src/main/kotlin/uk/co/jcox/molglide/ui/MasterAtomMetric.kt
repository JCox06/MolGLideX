package uk.co.jcox.molglide.ui

data class MasterAtomMetric(
    //Drawing positions for the selection box, and for the text
    //As for some reason, each has a different origin upon drawing
    val centreTextWidth: Double,
    val centreTextHeight: Double,
    val centreBoxWidth: Double,
    val centreBoxHeight: Double,

    //The size of the master atom to draw
    val textWidth: Int,
    val textHeight: Int,
)
