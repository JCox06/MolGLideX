package uk.co.jcox.molglide.editor.control

import org.openscience.cdk.config.Isotopes
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

        if (isOverride(label)) {
            return createCustomOverride(chemAtom, label)
        }

        if (Isotopes.getInstance().isElement(label)) {
            atomReplacement(chemAtom, label)
            return true
        }

        return parseComplexLabel(chemAtom, label)
    }

    private fun atomReplacement(chemAtom: ChemAtom, label: String) {
        val replaceAtomAction = ReplaceAtomAction(chemAtom, label)
        actionManager.executeAction(replaceAtomAction)
    }


    private fun createCustomOverride(chemAtom: ChemAtom, label: String): Boolean {
        val overrideFunc = overrideRegistry[label] ?: return false
        val action = ApplyAtomOverrideAction(chemAtom, label, overrideFunc)
        actionManager.executeAction(action)
        return true
    }


    private fun parseComplexLabel(chemAtom: ChemAtom, label: String): Boolean {
        TODO()
    }

    companion object {

        private val makeMe: (chemMolecule: ChemMolecule, chemAtom: ChemAtom) -> Unit = { chemMolecule, chemAtom ->
            chemAtom.setSymbol("C")
            chemAtom.setSymbolOverride("Me")
        }

        private val makeEt: (chemMolecule: ChemMolecule, chemAtom: ChemAtom) -> Unit = { chemMolecule, chemAtom ->
            chemAtom.setSymbol("C")
            val A1 = chemMolecule.addInternalAtom("C")
            chemMolecule.formBasicConnection(chemAtom, A1)
        }

        private val makePr: (chemMolecule: ChemMolecule, chemAtom: ChemAtom) -> Unit = { chemMolecule, chemAtom ->
            chemAtom.setSymbol("C")
            val A1 = chemMolecule.addInternalAtom("C")
            val A2 = chemMolecule.addInternalAtom("C")
            chemMolecule.formBasicConnection(chemAtom, A1)
            chemMolecule.formBasicConnection(A1, A2)
        }

        private val makeIsoPr: (chemMolecule: ChemMolecule, chemAtom: ChemAtom) -> Unit = { chemMolecule, chemAtom ->
            chemAtom.setSymbol("C")
            val A1 = chemMolecule.addInternalAtom("C")
            val A2 = chemMolecule.addInternalAtom("C")
            chemMolecule.formBasicConnection(chemAtom, A1)
            chemMolecule.formBasicConnection(chemAtom, A2)
        }

        private val makeBu: (chemMolecule: ChemMolecule, chemAtom: ChemAtom) -> Unit = { chemMolecule, chemAtom ->
            chemAtom.setSymbol("C")
            val A1 = chemMolecule.addInternalAtom("C")
            val A2 = chemMolecule.addInternalAtom("C")
            val A3 = chemMolecule.addInternalAtom("C")
            chemMolecule.formBasicConnection(chemAtom, A1)
            chemMolecule.formBasicConnection(A1, A2)
            chemMolecule.formBasicConnection(A2, A3)
        }

        private val makeSecBu: (chemMolecule: ChemMolecule, chemAtom: ChemAtom) -> Unit = { chemMolecule, chemAtom ->
            chemAtom.setSymbol("C")
            val A1 = chemMolecule.addInternalAtom("C")
            val A2 = chemMolecule.addInternalAtom("C")
            val A3 = chemMolecule.addInternalAtom("C")
            chemMolecule.formBasicConnection(chemAtom, A1)
            chemMolecule.formBasicConnection(A1, A2)
            chemMolecule.formBasicConnection(chemAtom, A3)
        }

        private val makeIsoBu: (chemMolecule: ChemMolecule, chemAtom: ChemAtom) -> Unit = { chemMolecule, chemAtom ->
            chemAtom.setSymbol("C")
            val A1 = chemMolecule.addInternalAtom("C")
            val A2 = chemMolecule.addInternalAtom("C")
            val A3 = chemMolecule.addInternalAtom("C")
            chemMolecule.formBasicConnection(chemAtom, A1)
            chemMolecule.formBasicConnection(A1, A2)
            chemMolecule.formBasicConnection(A1, A3)
        }

        private val makeTertBu: (chemMolecule: ChemMolecule, chemAtom: ChemAtom) -> Unit = { chemMolecule, chemAtom ->
            chemAtom.setSymbol("C")
            val A1 = chemMolecule.addInternalAtom("C")
            val A2 = chemMolecule.addInternalAtom("C")
            val A3 = chemMolecule.addInternalAtom("C")
            chemMolecule.formBasicConnection(chemAtom, A1)
            chemMolecule.formBasicConnection(chemAtom, A2)
            chemMolecule.formBasicConnection(chemAtom, A3)
        }

        private val makePh: (chemMolecule: ChemMolecule, chemAtom: ChemAtom) -> Unit = { chemMolecule, chemAtom ->
            chemAtom.setSymbol("C")
            val A1 = chemMolecule.addInternalAtom("C")
            val A2 = chemMolecule.addInternalAtom("C")
            val A3 = chemMolecule.addInternalAtom("C")
            val A4 = chemMolecule.addInternalAtom("C")
            val A5 = chemMolecule.addInternalAtom("C")

            val B1 = chemMolecule.formBasicConnection(chemAtom, A1)
            val B2 = chemMolecule.formBasicConnection(A1, A2)
            val B3 = chemMolecule.formBasicConnection(A2, A3)
            val B4 = chemMolecule.formBasicConnection(A3, A4)
            val B5 = chemMolecule.formBasicConnection(A4, A5)
            val B6 = chemMolecule.formBasicConnection(A5, chemAtom)

            chemMolecule.updateAromaticity(B1, true)
            chemMolecule.updateAromaticity(B2, true)
            chemMolecule.updateAromaticity(B3, true)
            chemMolecule.updateAromaticity(B4, true)
            chemMolecule.updateAromaticity(B5, true)
            chemMolecule.updateAromaticity(B6, true)

            chemMolecule.updateBondOrder(B1, 2)
            chemMolecule.updateBondOrder(B2, 1)
            chemMolecule.updateBondOrder(B3, 2)
            chemMolecule.updateBondOrder(B4, 1)
            chemMolecule.updateBondOrder(B5, 2)
            chemMolecule.updateBondOrder(B6, 1)
        }

        private val overrideRegistry = mapOf(
            "Me" to makeMe,
            "Et" to makeEt,
            "Pr" to makePr,
            "nPr" to makePr,
            "iPr" to makeIsoPr,
            "Bu" to makeBu,
            "nBu" to makeBu,
            "sBu" to makeSecBu,
            "iBu" to makeIsoBu,
            "tBu" to makeTertBu,
            "Ph" to makePh,
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