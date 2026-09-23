package uk.co.jcox.molglide.editor.io

import kotlinx.serialization.json.Json
import uk.co.jcox.molglide.editor.control.ActionManager
import uk.co.jcox.molglide.editor.control.actions.*
import uk.co.jcox.molglide.editor.model.EditorStateData
import uk.co.jcox.molglide.editor.model.chemengine.MgxAtom
import java.io.File
import java.io.IOException

class LevelLoader {

    private val idChemAtomMap: MutableMap<Int, MgxAtom> = mutableMapOf()
    var metaData: MolGLideMetaData = MolGLideMetaData()
    private set

    fun loadLevel(file: File): EditorStateData {

        if (!file.exists()) {
            throw IOException("File for loading does not exist!")
        }
        val jsonString = file.readText()
        return loadLevel(jsonString)
    }


    fun loadLevel(jsonString: String): EditorStateData {
        val dataSaveFile = Json.decodeFromString<DataSaveFile>(jsonString)
        val level = reconstructLevel(dataSaveFile)
        metaData = dataSaveFile.metaData
        return level
    }


    private fun reconstructLevel(dataSaveFile: DataSaveFile) : EditorStateData {
        val levelData = EditorStateData()
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
                val atomA = idChemAtomMap[dataBond.atomA] ?: return@forEach
                val atomB = idChemAtomMap[dataBond.atomB] ?: return@forEach

                val directBondConnectionAction = DirectBondConnectionAction(dataBond, chemMolecule, atomA, atomB)
                actionManager.executeAction(directBondConnectionAction)
            }
            chemMolecule.calculateChemData()
        }
        saveFile.arrows.forEach { dataArrow ->
            val action = DirectAddArrowAction(dataArrow)
            actionManager.executeAction(action)
        }

        saveFile.charges.forEach { dataCharge ->
            val chemAtom = idChemAtomMap[dataCharge.atomRef] ?: return@forEach
            val chemFormalCharge = chemAtom.createFormalChargeAt(dataCharge.position.x, dataCharge.position.y)
            chemFormalCharge.setCharge(dataCharge.charge)
            val action = CreateFormalCharge(chemFormalCharge)
            actionManager.executeAction(action)
        }
    }
}