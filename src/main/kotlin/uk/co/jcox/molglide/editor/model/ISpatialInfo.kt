package uk.co.jcox.molglide.editor.model

import org.joml.Vector2d

interface ISpatialInfo {

    fun getAllCoordinates(): Map<Int, Vector2d>
    fun pushNewCoordinates(coordinateMap: Map<Int, Vector2d>)
}