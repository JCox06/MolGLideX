package uk.co.jcox.molglide.editor.model.util

import org.joml.Vector2d
import uk.co.jcox.molglide.editor.model.ChemAtom
import uk.co.jcox.molglide.editor.model.ChemMolecule

class AtomPosSnapshot (
    molecule: ChemMolecule,
) {

    private val posMap: MutableMap<ChemAtom, Vector2d> = mutableMapOf()

    init {
        takeSnapshot(molecule)
    }


    fun takeSnapshot(chemMolecule: ChemMolecule) {
        chemMolecule.atoms().forEach { chemAtom ->
            posMap[chemAtom] = chemAtom.getPos()
        }
    }

    operator fun get(chemAtom: ChemAtom): Vector2d {
        return posMap[chemAtom] ?: throw NullPointerException("Cannot provide a position which was not supplied")
    }
}