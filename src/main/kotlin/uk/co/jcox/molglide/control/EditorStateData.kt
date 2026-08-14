package uk.co.jcox.molglide.control

import org.apache.jena.sparql.pfunction.library.container
import org.openscience.cdk.interfaces.IAtom


class EditorStateData (
    /**
     * Has nothing to do with the actual data
     * Just handy to use while the level is loaded
     * When saved, the currentID is removed, and will be loaded with a potentially
     * different next time
     */
    val currentID: Int,
    private val molecules: MutableList<ChemMolecule> = mutableListOf(),
    ) {

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
}
