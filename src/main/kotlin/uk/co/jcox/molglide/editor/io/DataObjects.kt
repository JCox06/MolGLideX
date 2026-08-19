package uk.co.jcox.molglide.editor.io

import kotlinx.serialization.Serializable
import uk.co.jcox.molglide.StereoChem
import uk.co.jcox.molglide.editor.model.ChemMolecule


@Serializable
data class DataSaveFile (
    val metaData: MolGLideMetaData = MolGLideMetaData(),
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
    val symbol: String,
    val isVisible: Boolean,
    val hydrogenPos: ChemMolecule.TrailingGroupPosition,
    val worldX: Double,
    val worldY: Double,
    val ignoreErrors: Boolean = false,
)

@Serializable
data class BondDataObject (
    val atomA: Int,
    val atomB: Int,
    val doubleFlip: Boolean,
    val order: Int,
    val stereoDisplay: StereoChem,
    val aromatic: Boolean,
)



@Serializable
data class MolGLideMetaData (
    val copyAtScreenX: Int = 0,
    val copyAtScreenY: Int = 0,
)

data class DataObjectIDMap (
    val chemMolecules: MutableMap<ChemMolecule, Int> = mutableMapOf(),
    val chemAtoms: MutableMap<ChemMolecule.ChemAtom, Int> = mutableMapOf(),
    val chemBonds: MutableMap<ChemMolecule.ChemBond, Int> = mutableMapOf(),
)

