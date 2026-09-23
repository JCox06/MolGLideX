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

    /**
     * Recalculates the positions of all atoms to produce a clean 2D representation of the molecule
     * The molecule is copied, and the cleaned version is returned as a copy
     * @return the copy of the molecule
     */
    fun clean2DStructure(): MgxMolecule

    fun indexOf(mgxAtom: MgxAtom): Int

    fun indexOf(mgxBond: MgxBond): Int

    fun copyAndMerge(mergeWith: MgxMolecule) : MgxMolecule

    fun isDisconnected(): Boolean

    fun splitIntoFragments(): Collection<MgxMolecule>

    fun getTemplateBuilder(): MgxTemplateBuilder

    /**
     * @return the centre of the molecule based on atom positions
     */
    fun getSpatialCentre(): Vector2d

    fun getBond(mgxAtomA: MgxAtom, mgxAtomB: MgxAtom): MgxBond

    fun hasBond(mgxAtomA: MgxAtom, mgxAtomB: MgxAtom): Boolean

    /**
     * Setting a molecule to transient flips all the transient sitting
     * in the bonds and atoms that make up this molecule
     */
    fun bulkSetTransient(transient: Boolean)

    fun inRing(mgxBond: MgxBond): Boolean

    fun createRingFragments(): Collection<MgxMolecule>

    fun getFormulaString(): String

    fun getMolecularMass(): Double

}