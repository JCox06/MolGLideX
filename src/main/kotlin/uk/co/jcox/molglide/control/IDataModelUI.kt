package uk.co.jcox.molglide.control

interface IDataModelUI {

    fun cameraX(): Double
    fun cameraY(): Double
    fun cameraZoom(): Double
    fun shouldPauseEvents(): Boolean

    fun getUIComponents(): Collection<AbstractUIComponent>

    fun getLastMouseX(): Int
    fun getLastMouseY(): Int

    fun getTransientSelectionStartX(): Int
    fun getTransientSelectionStartY(): Int
    fun getTransientSelectionAdvX(): Int
    fun getTransientSelectionAdvY(): Int
}