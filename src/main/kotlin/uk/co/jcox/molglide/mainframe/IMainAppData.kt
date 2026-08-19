package uk.co.jcox.molglide.mainframe

import uk.co.jcox.molglide.EditMode

interface IMainAppData {

    fun getEditMode(): EditMode

    fun getSelectedFormula(): String

    fun getSelectedWeight(): Double

    fun getSelectedHybrid(): String
}