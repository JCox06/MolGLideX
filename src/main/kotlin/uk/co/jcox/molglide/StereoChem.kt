package uk.co.jcox.molglide

import org.openscience.cdk.interfaces.IBond

enum class StereoChem (val cdk: IBond.Display) {
    WEDGED(IBond.Display.WedgeBegin),
    DASHED(IBond.Display.WedgedHashBegin),
    NORMAL(IBond.Display.Solid),
    ;

    companion object {
        fun getType(toFind: IBond.Display): StereoChem {
            val found = entries.find { it.cdk ==  toFind}
            if (found == null) {
                throw UnsupportedOperationException("MolGLide does not currently support this display type")
            }
            return found
        }
    }
}