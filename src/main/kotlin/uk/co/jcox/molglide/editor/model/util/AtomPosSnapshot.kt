package uk.co.jcox.molglide.editor.model.util

import org.joml.Vector2d
import uk.co.jcox.molglide.editor.model.ChemAtom
import uk.co.jcox.molglide.editor.model.ChemMolecule

class AtomPosSnapshot (
    atomList: List<ChemAtom>
) {

    private val posMap: MutableMap<ChemAtom, Vector2d> = mutableMapOf()

    init {
        takeSnapshot(atomList)
    }


    fun takeSnapshot(atomList: List<ChemAtom>) {
        atomList.forEach { chemAtom ->
            posMap[chemAtom] = chemAtom.getPos()
        }
    }

    operator fun get(chemAtom: ChemAtom): Vector2d {
        return posMap[chemAtom] ?: throw NullPointerException("Cannot provide a position which was not supplied")
    }

    fun map(): Map<ChemAtom, Vector2d> {
        return posMap
    }

    fun restoreSnapshot(chemMolecule: ChemMolecule) {
        chemMolecule.atoms().forEach { chemAtom ->
            val originalPos = posMap[chemAtom] ?: return@forEach
            chemAtom.atom.point2d.x = originalPos.x
            chemAtom.atom.point2d.y = originalPos.y
        }
    }

    companion object {
        fun ofMolecule(chemMolecule: ChemMolecule): AtomPosSnapshot {
            return AtomPosSnapshot(chemMolecule.atoms())
        }
    }
}