package uk.co.jcox.molglide.editor.model

import org.checkerframework.checker.units.qual.mol
import kotlin.math.max


class EditorStateData (

    private val molecules: MutableList<ChemMolecule> = mutableListOf(),
    private val arrows: MutableList<ChemArrow> = mutableListOf()
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

    var sessionID: String = ""


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

    fun getAtoms() : List<ChemAtom> {
        val atoms = mutableListOf<ChemAtom>()
        molecules.forEach {mol ->
            atoms.addAll(mol.atoms())
        }
        return atoms
    }

    fun getBonds() : List<ChemBond> {
        val bonds = mutableListOf<ChemBond>()
        molecules.forEach {mol ->
            bonds.addAll(mol.bonds())
        }
        return bonds
    }

    fun getSelectables(): List<IEditorSelectable> {
        val selectables = mutableListOf<IEditorSelectable>()
        molecules.forEach { chemMolecule ->
            selectables.addAll(chemMolecule.selectables())
        }
        selectables.addAll(arrows)
        return selectables
    }

    fun getSpatials(): List<ISpatialInfo> {
        val spatials = mutableListOf<ISpatialInfo>()
        molecules.forEach { chemMolecule ->
            spatials.addAll(chemMolecule.atoms())
        }
        spatials.addAll(arrows)
        return spatials
    }

    fun getArrows(): List<ChemArrow> = arrows

    fun addArrows(newArrows: List<ChemArrow>) {
        arrows.addAll(newArrows)
    }

    fun removeArrows(removeArrows: List<ChemArrow>) {
        arrows.removeAll(removeArrows)
    }

    fun getMolecules() : List<ChemMolecule> = molecules

    fun addArrow(chemArrow: ChemArrow) {
        arrows.add(chemArrow)
    }

    fun removeArrow(chemArrow: ChemArrow) {
        arrows.remove(chemArrow)
    }

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

    override fun getUIComponents(): Collection<AbstractUIComponent> {
        return uiDataBuilder.getUIData()
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
