package uk.co.jcox.molglide.editor.model.chemengine

import org.joml.Vector2d

interface ISpatialInfo {

    /**
     * NOTE: The params supplied are modified!
     */
    fun getAllCoordinates(): Map<Int, Vector2d>
    fun pushNewCoordinates(coordinateMap: Map<Int, Vector2d>)

    /**
     * The UIDataBuilder builder does not normally build the UI each frame
     * but instead only builds the full UI on each mouse click.
     *
     * Setting a component to transient will tell the UIDataBuilder that this specific component
     * needs rebuilding on every frame
     *
     * This allows for smooth movement of atoms, bonds, and other components.
     */
    fun setTransient(transient: Boolean)

    /**
     * @return whether this component should update each frame (see above)
     */
    fun isTransient(): Boolean
}