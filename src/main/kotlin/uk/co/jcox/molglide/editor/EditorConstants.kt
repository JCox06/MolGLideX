package uk.co.jcox.molglide.editor

import uk.co.jcox.molglide.editor.control.tool.AtomBondTool


object EditorConstants {

    //EDITOR APPEARANCE--------------------------------
    const val TEXT_SIZE = 32.0f //Text Size
    const val LINE_STROKE = 3.0f //Width of line
    const val BOND_MARKER = TEXT_SIZE * 0.5f //Square selection between atoms in a bond
    const val INTER_BOND_DISTANCE = 6.0 //Distance between the parallel strokes of a double or triple bond
    const val DEFAULT_BOND_DISTANCE = 50.0 //Length for all bonds
    const val INTER_HASH_DISTANCE = DEFAULT_BOND_DISTANCE / 10.0
    const val HASH_DISTANCE = DEFAULT_BOND_DISTANCE / 3.0
    const val ARROW_WIDTH = INTER_HASH_DISTANCE / 2
    const val ARROW_LENGTH = ARROW_WIDTH * 2


    //EDITOR BEHAVIOUR--------------------------------
    const val MOUSE_SENSE_MOVE = 2.0 //How fast you can pan around the editor
    const val MOUSE_SENSE_ZOOM = 0.5 //How fast you can zoom the editor
    const val SIG_MOUSE_DELTA = 2.0f //How much the mouse has to move before it moves "significantly"

    val COMMON_ANGLES = listOf<Float>(
        //Cardinal directions
        0.0f, 90.0f, -90.0f, 180.0f, -180.0f,

        //Semi Cardinal directions
        45.0f, 135.0f, -45.0f, -135.0f,

        //Odd angles - For triangles
        30.0f, -30.0f, 60.0f, -60.0f, 120.0f, -120.0f, 150.0f, -150.0f,

        //For Pentagons
        108.0f, -108.0f, 72.0f, -72.0f, 36.0f, -36.0f, 126.0f, -126.0f, 144.0f, -144.0f,
        18.0f, -18.0f, 162.0f, -162.0f, 126.0f, -126.0f, 36.0f, -36.0f, 54.0f, -54.0f,
    )
}