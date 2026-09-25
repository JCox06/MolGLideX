package uk.co.jcox.molglide.editor.model.chemengine

interface MgxTemplateBuilder {

    /**
     * Place atoms in a ring in the associated molecule
     * @param vertexCount the ring size
     */
    fun buildIsolatedOrganicRing(vertexCount: Int, centreX: Double, centreY: Double, bondLength: Double)

    fun buildIsolatedBenzene(centreX: Double, centreY: Double, bondLength: Double)

    fun buildCommonOrganicRing(vertexCount: Int, commonAtom: MgxAtom)

    fun buildCommonOrganicRing(vertexCount: Int, commonBond: MgxBond)
}