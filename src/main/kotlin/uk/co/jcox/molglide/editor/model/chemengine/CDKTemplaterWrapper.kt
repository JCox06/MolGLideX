package uk.co.jcox.molglide.editor.model.chemengine

import org.openscience.cdk.Ring
import org.openscience.cdk.interfaces.IBond
import org.openscience.cdk.interfaces.IRing
import org.openscience.cdk.layout.RingPlacer
import javax.vecmath.Point2d

class CDKTemplaterWrapper (private val cdkWrapper: CDKContainerWrapper) : MgxTemplateBuilder {

    private val cdkAtomContainer = cdkWrapper.getHandle()
    private val ringBuilder = RingPlacer()


    private fun buildCDKRing(vertexCount: Int, centreX: Double, centreY: Double, bondLength: Double): IRing {
        val newRing = Ring(vertexCount, "C")
        ringBuilder.placeRing(newRing, Point2d(centreX, centreY), bondLength)
        return newRing
    }


    override fun buildIsolatedOrganicRing(vertexCount: Int, centreX: Double, centreY: Double, bondLength: Double) {
        val ring = buildCDKRing(vertexCount, centreX, centreY, bondLength)
        cdkWrapper.addRawCDKData(ring)
    }

    override fun buildIsolatedBenzene(centreX: Double, centreY: Double, bondLength: Double) {
        val ring = buildCDKRing(6, centreX, centreY, bondLength)
        for ((index, bond) in ring.bonds().withIndex()) {
            val makeSingle = index % 2 == 0
            bond.setIsAromatic(true)
            if (makeSingle) {
                bond.order = IBond.Order.SINGLE
            } else {
                bond.order = IBond.Order.DOUBLE
            }
        }
        cdkWrapper.addRawCDKData(ring)
    }
}