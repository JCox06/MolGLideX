package uk.co.jcox.molglide.editor.model.chemengine

interface MgxTemplateBuilder {

    fun buildIsolatedOrganicRing(vertexCount: Int, centreX: Double, centreY: Double, bondLength: Double)

    fun buildIsolatedBenzene(centreX: Double, centreY: Double, bondLength: Double)
}