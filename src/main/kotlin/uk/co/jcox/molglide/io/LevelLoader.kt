package uk.co.jcox.molglide.io

import kotlinx.serialization.json.Json
import org.apache.jena.sparql.function.library.date
import uk.co.jcox.molglide.control.ActionManager
import uk.co.jcox.molglide.control.ChemMolecule
import uk.co.jcox.molglide.control.EditorStateData
import uk.co.jcox.molglide.control.actions.DirectAtomCreationAction
import uk.co.jcox.molglide.control.actions.DirectBondConnectionAction
import uk.co.jcox.molglide.control.actions.DirectMoleculeCreationAction
import java.io.File
import java.io.IO
import java.io.IOException

class LevelLoader {

    private val idChemAtomMap: MutableMap<Int, ChemMolecule.ChemAtom> = mutableMapOf()
    var metaData: MolGLideMetaData = MolGLideMetaData()
    private set

    fun loadLevel(file: File, id: Int): EditorStateData {

        if (!file.exists()) {
            throw IOException("File for loading does not exist!")
        }
        val jsonString = file.readText()
        return loadLevel(jsonString, id)
    }


    fun loadLevel(jsonString: String, id: Int): EditorStateData {
        val dataSaveFile = Json.decodeFromString<DataSaveFile>(jsonString)
        val level = reconstructLevel(dataSaveFile, id)
        metaData = dataSaveFile.metaData
        return level
    }


    private fun reconstructLevel(dataSaveFile: DataSaveFile, id: Int) : EditorStateData {
        val levelData = EditorStateData(id)
        val levelActionBuilder = ActionManager(levelData)

        runDirectDataActions(dataSaveFile, levelActionBuilder)

        return levelData
    }

    private fun runDirectDataActions(saveFile: DataSaveFile, actionManager: ActionManager) {
        saveFile.dataMolecules.forEach { dataMolecule ->
           val directMoleculeCreationAction = DirectMoleculeCreationAction()
           actionManager.executeAction(directMoleculeCreationAction)
            val chemMolecule = directMoleculeCreationAction.newMolecule

            dataMolecule.atoms.forEach { atomID ->
                val dataAtom = saveFile.dataAtoms[atomID] ?: throw IOException("Save file was corrupted - Unable to open")
                val directlyAddAtomAction = DirectAtomCreationAction(chemMolecule, dataAtom)
                actionManager.executeAction(directlyAddAtomAction)
                idChemAtomMap[dataAtom.loaderID] = directlyAddAtomAction.newChemAtom
            }

            dataMolecule.bonds.forEach { bondID ->
                val dataBond = saveFile.dataBonds[bondID] ?: throw IOException("Save file was corrupted - Unable to open")
                val atomA = idChemAtomMap[dataBond.atomA] ?: throw IOException("Save file was corrupted - Unable to open")
                val atomB = idChemAtomMap[dataBond.atomB] ?: throw IOException("Save file was corrupted - Unable to open")

                val directBondConnectionAction = DirectBondConnectionAction(dataBond, chemMolecule, atomA, atomB)
                actionManager.executeAction(directBondConnectionAction)
            }

            chemMolecule.calculateAtomProperties()
        }
    }
}