package uk.co.jcox.molglide.control

interface IDataModelUI {

    fun cameraX(): Double
    fun cameraY(): Double
    fun cameraZoom(): Double
    fun shouldPauseEvents(): Boolean

    fun getLineData(): List<UILine>
    fun getTriangleData(): List<UITriangle>
    fun getAtomData(): List<UIAtom>
    fun getBondData(): List<UIBond>

    fun getLastMouseX(): Int
    fun getLastMouseY(): Int

    fun getTransientSelectionStartX(): Int
    fun getTransientSelectionStartY(): Int
    fun getTransientSelectionAdvX(): Int
    fun getTransientSelectionAdvY(): Int
}