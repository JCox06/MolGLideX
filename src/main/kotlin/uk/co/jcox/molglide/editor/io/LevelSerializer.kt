package uk.co.jcox.molglide.editor.io

import kotlinx.serialization.json.Json
import uk.co.jcox.molglide.editor.model.*
import java.io.IOException

class LevelSerializer {


    /**
     * Returns the JSON MGX file of the current editor state
     * @param saveData The data to save
     * @param batchSelection If not null, acts as a filter
     */
    fun getJSONEncoding(saveData: EditorStateData, metaData: MolGLideMetaData = MolGLideMetaData(), selected: Collection<IEditorSelectable>? = null) : String {
        val dataSaveFile = saveEditorState(saveData, metaData, selected)
        val result = Json.encodeToString(dataSaveFile)
        return result
    }

    fun saveEditorState(stateData: EditorStateData, metaData: MolGLideMetaData, selected: Collection<IEditorSelectable>? = null): DataSaveFile {
        val saveFile = DataSaveFile(metaData)
        val idMappings = generateLevelIDs(stateData)

        stateData.getMolecules().forEach { molecule ->
            serializeMolecule(saveFile, idMappings, molecule, selected)
        }

        stateData.getArrows().forEach { chemArrow ->
            if (!checkShouldSerialize(chemArrow, selected)) {
                return@forEach
            }
            serializeArrow(stateData, saveFile, chemArrow)
        }

        stateData.getCharges().forEach { chemFc ->
            if (!checkShouldSerialize(chemFc, selected)) {
                return@forEach
            }
            serializeFormalCharge(saveFile, idMappings, chemFc)
        }
        return saveFile
    }


    private fun serializeMolecule(saveFile: DataSaveFile, idMappings: DataObjectIDMap, molecule: ChemMolecule, selected: Collection<IEditorSelectable>? = null) {
        val dataMolecule = MoleculeDataObject()
        var addMolecule = false

        molecule.atoms().forEach { chemAtom ->
            if (!checkShouldSerialize(chemAtom, selected)) {
                return@forEach
            }
            val id = idMappings.chemAtoms[chemAtom] ?: throw IOException("Level Data ID for atom is missing upon molecule serialization")
            val pos = chemAtom.getPos()
            val dataAtom = AtomDataObject(id, chemAtom.atom.symbol, chemAtom.isVisible(), chemAtom.getTrailPos(), pos.x, pos.y, chemAtom.shouldIgnoreErrors())
            dataMolecule.atoms.add(id)
            saveFile.dataAtoms[id] = dataAtom
            addMolecule = true
        }

        molecule.bonds().forEach { chemBond ->
            if (!checkShouldSerialize(chemBond, selected)) {
                return@forEach
            }
            val bondID = idMappings.chemBonds[chemBond] ?: throw IOException("Level Data ID for bond is missing upon molecule serialization")
            val atomAID = idMappings.chemAtoms[chemBond.getStart()] ?: throw IOException("Level Data ID for atom start of bond is missing upon molecule serialization")
            val atomBID = idMappings.chemAtoms[chemBond.getEnd()] ?: throw IOException("Level Data for ID for atom end of bond is missing upon molecule serialization")

            val dataBond = BondDataObject(atomAID, atomBID, chemBond.shouldFlip(), chemBond.bond.order.numeric(), chemBond.stereo(), chemBond.bond.isAromatic)

            dataMolecule.bonds.add(bondID)
            saveFile.dataBonds[bondID] = dataBond
            addMolecule = true
        }
        if (addMolecule) saveFile.dataMolecules.add(dataMolecule)
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


    private fun serializeArrow(stateData: EditorStateData, dataSaveFile: DataSaveFile, chemArrow: ChemArrow) {

        val vecMap = mutableMapOf<Int, VectorDataObject>()

        chemArrow.arrowPoints.forEach { id, jmolVec ->
            vecMap[id] = VectorDataObject(jmolVec.x, jmolVec.y)
        }

        val serialChemArrow = ArrowDataObject(vecMap, chemArrow.startArrow, chemArrow.endArrow)

        dataSaveFile.arrows.add(serialChemArrow)
    }


    private fun serializeFormalCharge(dataSaveFile: DataSaveFile, idMappings: DataObjectIDMap, fc: ChemFormalCharge) {
        val atomRef = idMappings.chemAtoms[fc.chemAtom] ?: return
        val p = fc.position
        val dataObject = FormalChargeObject(atomRef, fc.getCharge(), VectorDataObject(p.x, p.y))

        dataSaveFile.charges.add(dataObject)
    }

    private fun checkShouldSerialize(component: IEditorSelectable, selected: Collection<IEditorSelectable>?): Boolean {
        if (selected == null) {
            return true
        }
        if (selected.contains(component)) {
            return true
        }
        return false
    }
}