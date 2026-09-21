package uk.co.jcox.molglide.editor.control

import org.openscience.cdk.config.Isotopes
import org.openscience.cdk.interfaces.IAtomContainer
import org.openscience.cdk.templates.MoleculeFactory
import uk.co.jcox.molglide.editor.control.actions.ApplyAtomOverrideAction
import uk.co.jcox.molglide.editor.control.actions.ReplaceAtomAction
import uk.co.jcox.molglide.editor.model.ChemAtom
import uk.co.jcox.molglide.editor.model.ChemMolecule

class LabelEditor (
    private val actionManager: ActionManager
) {
    /**
     * Takes the user supplied string and attempts to apply a custom atom label
     * @param chemAtom The atom to apply the label to
     * @param label the custom label to parse
     *
     * @return true if the label was successfully applied or false if an error occurred
     */
    fun editLabel(chemAtom: ChemAtom, label: String): Boolean {

        if (Isotopes.getInstance().isElement(label)) {
            atomReplacement(chemAtom, label)
            return true
        }

        if (label == "Me") {
            createMethylGroup(chemAtom)
            return true
        }

        if (isOverride(label)) {
            return createCustomOverride(chemAtom, label)
        }

        return parseComplexLabel(chemAtom, label)
    }

    private fun atomReplacement(chemAtom: ChemAtom, label: String) {
        val replaceAtomAction = ReplaceAtomAction(chemAtom, label)
        actionManager.executeAction(replaceAtomAction)
    }


    private fun createMethylGroup(chemAtom: ChemAtom) {
        val action = ApplyAtomOverrideAction(chemAtom, "C", "Me", null)
        actionManager.executeAction(action)
    }

    private fun createCustomOverride(chemAtom: ChemAtom, label: String): Boolean {
        val chemDataFunc = overrideRegistry[label] ?: return false
        val chemData = chemDataFunc()
        val action = ApplyAtomOverrideAction(chemAtom, "C", label, chemData)
        actionManager.executeAction(action)
        return true
    }

    private fun parseComplexLabel(chemAtom: ChemAtom, label: String): Boolean {
        TODO()
    }

    companion object {

        //The first atom of the container is the joining/anchor

        private fun prepareGroup(atomContainer: IAtomContainer): ChemMolecule {
            val mol = ChemMolecule(atomContainer)
            mol.skipUIBuild()
            return mol
        }


        private val makeEt: () -> ChemMolecule = {
            val atomContainer = MoleculeFactory.makeAlkane(1)
            prepareGroup(atomContainer)
        }


        private val makePr: () -> ChemMolecule = {
            val atomContainer = MoleculeFactory.makeAlkane(2)
            prepareGroup(atomContainer)
        }

        private val makeBu: () -> ChemMolecule = {
            val atomContainer = MoleculeFactory.makeAlkane(3)
            prepareGroup(atomContainer)
        }

        //todo FIX: Make the first atom in the new container the one to replace all other containers!

        private val makePh: () -> ChemMolecule = {
            val atomContainer = MoleculeFactory.makeBenzene()
            prepareGroup(atomContainer)
        }

        private val overrideRegistry = mapOf(
            "Et" to makeEt,
            "Pr" to makePr,
            "Bu" to makeBu,
            "nBu" to makeBu,
            "Ph" to makePh
        )


        /**
         * There are two types of labels that can be applied to atoms in MolGLide:
         *
         *  a) Label Overrides: These are simple labels in which the main atom is given
         *  custom text that overrides the symbol.
         *
         *  When an override group is added, CDK IAtoms and IBonds are created to match the group content, however
         *  these are flagged specifically as to be hidden in the editor, and they do not have any spatial coordinates
         *  associated with them
         *
         *  An override group is actually directly a ChemAtom/CDK IAtom, and hence the editor will treat
         *  overrides exactly like atoms.
         *
         *  Examples: Me, Et, Pr, Bu, tBu, Ph
         *
         *  b) Complex Labels: A complex string like OMe, OEt, CH2CH3, is interpreted as a list of
         *  connected ChemAtoms. The positions of the ChemAtoms are "locked" and managed by an AtomLabelGroup
         *  which asks the renderer for font metrics so it knows where to place each element.
         *  Label overrides within complex groups are treated as label overrides as explained above.
         *
         *
         *  --------------------------------------------------------
         *  CDK SGroups - Although this functionality (I think) exists in the CDK as SGroups,
         *  I am not sure how to use them.
         *
         *  @param label checks if the label is contained in the override registry.
         *  Note: Me is an override, but not present in the registry
         */
        private fun isOverride(label: String): Boolean {
            return overrideRegistry.containsKey(label)
        }
    }
}