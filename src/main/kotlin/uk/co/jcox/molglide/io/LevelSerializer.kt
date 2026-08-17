package uk.co.jcox.molglide.io

import kotlinx.serialization.json.Json
import org.checkerframework.checker.units.qual.m
import uk.co.jcox.molglide.control.ChemMolecule
import uk.co.jcox.molglide.control.EditorStateData
import java.io.IOException

class LevelSerializer {


    fun getJSONEncoding(saveData: EditorStateData) : String {
        val dataSaveFile = saveEditorState(saveData)
        val result = Json.encodeToString(dataSaveFile)
        return result
    }

    fun saveEditorState(stateData: EditorStateData): DataSaveFile {
        val saveFile = DataSaveFile()
        val idMappings = generateLevelIDs(stateData)

        stateData.getMolecules().forEach { molecule ->
            serializeMolecule(saveFile, idMappings, molecule)
        }
        return saveFile
    }


    private fun serializeMolecule(saveFile: DataSaveFile, idMappings: DataObjectIDMap, molecule: ChemMolecule) {
        val dataMolecule = MoleculeDataObject()
        saveFile.dataMolecules.add(dataMolecule)

        molecule.atoms().forEach { chemAtom ->
            val id = idMappings.chemAtoms[chemAtom] ?: throw IOException("Level Data ID for atom is missing upon molecule serialization")
            val pos = chemAtom.getPos()
            val dataAtom = AtomDataObject(id, chemAtom.atom.id, chemAtom.atom.symbol, chemAtom.isVisible(), chemAtom.getTrailPos(), pos.x, pos.y, chemAtom.shouldIgnoreErrors())
            dataMolecule.atoms.add(id)
            saveFile.dataAtoms[id] = dataAtom
        }

        molecule.bonds().forEach { chemBond ->
            val bondID = idMappings.chemBonds[chemBond] ?: throw IOException("Level Data ID for bond is missing upon molecule serialization")
            val atomAID = idMappings.chemAtoms[chemBond.getStart()] ?: throw IOException("Level Data ID for atom start of bond is missing upon molecule serialization")
            val atomBID = idMappings.chemAtoms[chemBond.getEnd()] ?: throw IOException("Level Data for ID for atom end of bond is missing upon molecule serialization")

            val dataBond = BondDataObject(chemBond.bond.id, atomAID, atomBID, chemBond.shouldFlip(), chemBond.bond.order.numeric(), chemBond.stereo(), chemBond.bond.isAromatic)

            dataMolecule.bonds.add(bondID)
            saveFile.dataBonds[bondID] = dataBond
        }
    }

    private fun generateLevelIDs(stateData: EditorStateData): DataObjectIDMap {
        var atomID = 0
        var bondID = 0
        var moleID = 0
        val dataMapping = DataObjectIDMap()

        stateData.getMolecules().forEach { molecule ->
            dataMapping.chemMolecules[molecule] = moleID++

            molecule.atoms().forEach { atom ->
                dataMapping.chemAtoms[atom] = atomID++
            }

            molecule.bonds().forEach { bond ->
                dataMapping.chemBonds[bond] = bondID++
            }
        }
        return dataMapping
    }
}