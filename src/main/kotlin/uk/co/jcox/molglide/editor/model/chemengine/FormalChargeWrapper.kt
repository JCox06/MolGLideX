package uk.co.jcox.molglide.editor.model.chemengine

import org.joml.Vector2d
import org.xmlcml.euclid.Vector2

class FormalChargeWrapper(
    private val mgxAtom: MgxAtom,
    private var xPos: Double,
    private var yPos: Double,
) : MgxFormalCharge {

    override fun getCharge(): Int {
        return mgxAtom.getFormalCharge()
    }

    override fun setCharge(charge: Int) {
        mgxAtom.setFormalCharge(charge)
    }

    override fun getPos(): Vector2d {
        return Vector2d(xPos, yPos)
    }

    override fun setPos(xPos: Double, yPos: Double) {
        this.xPos = xPos
        this.yPos = yPos
    }

    override fun setTransient(transient: Boolean) {
        mgxAtom.setTransient(transient)
    }

    override fun isTransient(): Boolean {
        return mgxAtom.isTransient()
    }

    override fun getAssociatedAtom(): MgxAtom {
        return mgxAtom
    }
}