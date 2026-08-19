package uk.co.jcox.molglide.control

import com.sun.org.apache.xpath.internal.operations.Bool
import org.apache.jena.reasoner.rulesys.builtins.Max
import org.apache.jena.sparql.pfunction.library.container
import org.openscience.cdk.interfaces.IAtom
import kotlin.math.max


class EditorStateData (

    private val molecules: MutableList<ChemMolecule> = mutableListOf(),
    ) : IDataModelUI {

    var cameraX: Double = 0.0
    var cameraY: Double = 0.0
    var cameraZoom: Double = 1.0
        set(value) {field = max(1.0, value)}
    var pauseEvents: Boolean = false

    val selectionManager: SelectionManager = SelectionManager()
    val uiDataBuilder = UIDataBuilder(this, selectionManager)

    var mouseX: Int = 0
    var mouseY: Int = 0

    var canSelectBox: Boolean = true
    var transientBoxSelectStartX: Int = 0
    var transientBoxSelectStartY: Int = 0
    var transientBoxSelectAdvX: Int = 0
    var transientBoxSelectAdvY: Int = 0



    fun createMolecule(initialAtom: String, positionX: Int, positionY: Int) : ChemMolecule {
        //Create the new atom container
        val molecule: ChemMolecule = ChemMolecule()
        molecules.add(molecule)
        molecule.addAtom( initialAtom, positionX.toDouble(), positionY.toDouble())
        return molecule
    }

    fun addMolecule(mol: ChemMolecule) {
        molecules.add(mol)
    }

    fun addMolecules(extra: Collection<ChemMolecule>) {
        molecules.addAll(extra)
    }

    fun removeMolecule(molecule: ChemMolecule) {
        molecules.remove(molecule)
    }

    fun removeMolecules(remove: Collection<ChemMolecule>) {
        molecules.removeAll(remove)
    }

    fun getAtoms() : List<ChemMolecule.ChemAtom> {
        val atoms = mutableListOf<ChemMolecule.ChemAtom>()
        molecules.forEach {mol ->
            atoms.addAll(mol.atoms())
        }
        return atoms
    }

    fun getBonds() : List<ChemMolecule.ChemBond> {
        val bonds = mutableListOf<ChemMolecule.ChemBond>()
        molecules.forEach {mol ->
            bonds.addAll(mol.bonds())
        }
        return bonds
    }

    fun getMolecules() : List<ChemMolecule> = molecules

    override fun cameraX(): Double {
        return cameraX
    }

    override fun cameraY(): Double {
        return cameraY
    }

    override fun cameraZoom(): Double {
        return cameraZoom
    }

    override fun shouldPauseEvents(): Boolean {
        return pauseEvents
    }

    override fun getLineData(): List<UILine> {
        return uiDataBuilder.getUILines()
    }

    override fun getTriangleData(): List<UITriangle> {
        return uiDataBuilder.getUITriangles()
    }

    override fun getAtomData(): List<UIAtom> {
        return uiDataBuilder.getUIAtoms()
    }

    override fun getBondData(): List<UIBond> {
        return uiDataBuilder.getUIBonds()
    }

    override fun getLastMouseX(): Int {
        return mouseX
    }

    override fun getLastMouseY(): Int {
        return mouseY
    }

    override fun getTransientSelectionStartX(): Int {
        return transientBoxSelectStartX
    }

    override fun getTransientSelectionStartY(): Int {
        return transientBoxSelectStartY
    }

    override fun getTransientSelectionAdvX(): Int {
        return transientBoxSelectAdvX
    }

    override fun getTransientSelectionAdvY(): Int {
        return transientBoxSelectAdvY
    }
}
