package uk.co.jcox.molglide.io

import kotlinx.serialization.Serializable
import uk.co.jcox.molglide.StereoChem
import uk.co.jcox.molglide.control.ChemMolecule


@Serializable
data class DataSaveFile (
    val dataMolecules: MutableList<MoleculeDataObject> = mutableListOf(),
    val dataBonds: MutableMap<Int, BondDataObject> = mutableMapOf(),
    val dataAtoms: MutableMap<Int, AtomDataObject> = mutableMapOf(),
)

@Serializable
data class MoleculeDataObject (
    val atoms: MutableList<Int> = mutableListOf(),
    val bonds: MutableList<Int> = mutableListOf()
)

@Serializable
data class AtomDataObject (
    val loaderID: Int,
    val editorID: String,
    val symbol: String,
    val isVisible: Boolean,
    val hydrogenPos: ChemMolecule.TrailingGroupPosition,
    val worldX: Double,
    val worldY: Double,
)

@Serializable
data class BondDataObject (
    val editorID: String,
    val atomA: Int,
    val atomB: Int,
    val doubleFlip: Boolean,
    val order: Int,
    val stereoDisplay: StereoChem,
    val aromatic: Boolean,
)

data class DataObjectIDMap (
    val chemMolecules: MutableMap<ChemMolecule, Int> = mutableMapOf(),
    val chemAtoms: MutableMap<ChemMolecule.ChemAtom, Int> = mutableMapOf(),
    val chemBonds: MutableMap<ChemMolecule.ChemBond, Int> = mutableMapOf(),
)