package uk.co.jcox.molglide.editor.model

import org.joml.Vector2d

interface ISpatialInfo {

    /**
     * NOTE: The params supplied are modified!
     */
    fun getAllCoordinates(): Map<Int, Vector2d>
    fun pushNewCoordinates(coordinateMap: Map<Int, Vector2d>)
}