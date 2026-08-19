package uk.co.jcox.molglide

interface IMainAppData {

    fun getEditMode(): EditMode

    fun getSelectedFormula(): String

    fun getSelectedWeight(): Double

    fun getSelectedHybrid(): String
}