package uk.co.jcox.molglide.editor.model

import org.joml.Vector2d

interface IEditorSelectable {

    /**
     * Tells the selection manager where this object's "Centre" is
     */
    fun getSelectionPosition(): Vector2d
}