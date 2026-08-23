package uk.co.jcox.molglide.editor.io

import kotlinx.serialization.Serializable
import org.joml.Vector2d
import uk.co.jcox.molglide.StereoChem
import uk.co.jcox.molglide.editor.model.ChemArrow
import uk.co.jcox.molglide.editor.model.ChemAtom
import uk.co.jcox.molglide.editor.model.ChemBond
import uk.co.jcox.molglide.editor.model.ChemMolecule


@Serializable
data class DataSaveFile (
    val metaData: MolGLideMetaData = MolGLideMetaData(),
    val dataMolecules: MutableList<MoleculeDataObject> = mutableListOf(),
    val dataBonds: MutableMap<Int, BondDataObject> = mutableMapOf(),
    val dataAtoms: MutableMap<Int, AtomDataObject> = mutableMapOf(),
    val arrows: MutableList<ArrowDataObject> = mutableListOf()
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
data class ArrowDataObject (
    val points: Map<Int, VectorDataObject>,
    val arrowStart: ChemArrow.ArrowHead,
    val arrowEnd: ChemArrow.ArrowHead
)

@Serializable
data class VectorDataObject (
    val x: Double,
    val y: Double,
)

@Serializable
data class MolGLideMetaData (
    val copyAtScreenX: Int = 0,
    val copyAtScreenY: Int = 0,
)

data class DataObjectIDMap (
    val chemMolecules: MutableMap<ChemMolecule, Int> = mutableMapOf(),
    val chemAtoms: MutableMap<ChemAtom, Int> = mutableMapOf(),
    val chemBonds: MutableMap<ChemBond, Int> = mutableMapOf(),
)

