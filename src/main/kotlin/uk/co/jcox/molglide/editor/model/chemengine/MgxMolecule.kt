package uk.co.jcox.molglide.editor.model.chemengine

import org.joml.Vector2d

interface MgxMolecule {

    fun addAtom(element: String, xPos: Double = 0.0, yPos: Double = 0.0): MgxAtom

    fun addAtom(mgxAtom: MgxAtom)

    fun addBond(mgxAtomA: MgxAtom, mgxAtomB: MgxAtom, order: Int): MgxBond

    fun addBond(mgxBond: MgxBond)

    fun addBond(mgxIndexA: Int, mgxIndexB: Int, order: Int): MgxBond

    fun setBondOrder(mgxBond: MgxBond, order: Int)

    fun removeAtom(mgxAtom: MgxAtom)

    fun removeBond(mgxBond: MgxBond)

    fun changeAtomSymbol(mgxAtom: MgxAtom, element: String)

    fun getCanonicalSMILES(): String

    fun getInchi(): InchiReturn

    fun bonds(): Collection<MgxBond>

    fun atoms(): Collection<MgxAtom>

    fun bondCount(mgxAtom: MgxAtom): Int

    fun calculateChemData()

    fun selectables(): Collection<IEditorSelectable>

    fun clean2DStructure(): MgxMolecule

    fun indexOf(mgxAtom: MgxAtom): Int

    fun indexOf(mgxBond: MgxBond): Int

    fun copyAndMerge(mergeWith: MgxMolecule) : MgxMolecule

    fun isDisconnected(): Boolean

    fun splitIntoFragments(): Collection<MgxMolecule>

    fun getTemplateBuilder(): MgxTemplateBuilder

    fun getSpatialCentre(): Vector2d

    fun getBond(mgxAtomA: MgxAtom, mgxAtomB: MgxAtom): MgxBond

    fun hasBond(mgxAtomA: MgxAtom, mgxAtomB: MgxAtom): Boolean

    fun bulkSetTransient(transient: Boolean)

    fun inRing(mgxBond: MgxBond): Boolean

    fun createRingFragments(): Collection<MgxMolecule>

    fun getFormulaString(): String

    fun getMolecularMass(): Double
}