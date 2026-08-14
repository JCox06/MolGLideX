package uk.co.jcox.molglide.control.actions

import org.apache.jena.sparql.function.library.date
import org.openscience.cdk.Ring
import org.openscience.cdk.interfaces.IAtomContainer
import org.openscience.cdk.interfaces.IBond
import org.openscience.cdk.layout.RingPlacer
import uk.co.jcox.molglide.EditMode
import uk.co.jcox.molglide.control.ChemMolecule
import uk.co.jcox.molglide.control.EditorStateData
import uk.co.jcox.molglide.control.tool.AtomBondTool
import javax.vecmath.Point2d
import javax.vecmath.Vector2d

class RingCreatorAction (
    private val clickX: Int,
    private val clickY: Int,
    private val insert: EditMode,
) : IDataAction {

    private val ringBuilder = RingPlacer()
    lateinit var placedRing: ChemMolecule


    override fun execute(data: EditorStateData) {
        val newRing = Ring(insert.ringSize, "C")
        ringBuilder.placeRing(newRing, Point2d(clickX.toDouble(), clickY.toDouble()), AtomBondTool.CONNECTION_DISTANCE.toDouble())

        val moleculeToAdd: IAtomContainer = newRing
        val newChemMolecule = ChemMolecule(moleculeToAdd)
        newChemMolecule.atoms().forEach { it.setVisible(false) }

        if (insert == EditMode.RING_BENZENE) {
            specialHandlingForBenzene(newChemMolecule)
        }

        data.addMolecule(newChemMolecule)
        placedRing = newChemMolecule
    }


    fun getRingCentre(): Vector2d {
        var avgX = 0.0
        var avgY = 0.0
        placedRing.atoms().forEach { atom ->
            val pos = atom.getPos()
            avgX += pos.x
            avgY += pos.y
        }
        avgX /= placedRing.atoms().size
        avgY /= placedRing.atoms().size
        return Vector2d(avgX, avgY)
    }

    override fun undo(data: EditorStateData) {
        placedRing?.let { data.removeMolecule(it) }
    }

    override fun redo(data: EditorStateData) {
        placedRing?.let { data.addMolecule(it) }
    }

    private fun specialHandlingForBenzene(newRing: ChemMolecule) {
        var makeDouble = true
        newRing.bonds().forEach { bond ->
            if (makeDouble) {
                newRing.updateBondOrder(bond, IBond.Order.DOUBLE)
            }
            bond.bond.setIsAromatic(true)
            makeDouble = !makeDouble

        }
    }
}