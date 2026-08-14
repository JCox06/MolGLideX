package uk.co.jcox.molglide

import org.openscience.cdk.Isotope
import org.openscience.cdk.config.Elements
import org.openscience.cdk.config.IsotopeFactory

object LabelToSmiles {

    private val commonDictionary = mapOf(
        "Me" to "[*]C(H)(H)H",
        "Et" to "[*]CC",
        "Pr" to "[*]CCC",
        "Bu" to "[*]CCCC", //todo Add nBu, tBu and others at some point
        "NO2" to "[*]N(=O)(=O)",
        "CO" to "[*]C=O",
        "CO2H" to "[*](C=O)(O-H)",
    )


    /**
     * Take user input string and try to find the corresponding chemical structure
     * This class is currently very very basic - todo make it actually work well
     *
     * But for now, it can be tested to see how good the solution is
     *
     * @param customText Custom user text to match against an element or molecular fragment
     * @return conversion to SMILES (null if nothing found)
     */
    fun lookUp(customText: String) : String? {

        //First check to see if the label is an element
        //If it is an element, the string can just be returned
        if (isElement(customText)) {
            return customText
        }

        //Check if it is a well known group
        val common = commonDictionary[customText]
        return common
    }


    private fun isElement(text: String): Boolean {
        val element = Elements.ofString(text)
        if (element == Elements.Unknown) {
            return false
        }
        return true
    }

}