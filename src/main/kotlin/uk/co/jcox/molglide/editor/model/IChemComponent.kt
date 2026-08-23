package uk.co.jcox.molglide.editor.model


interface IChemComponent {

    fun isTransient(): Boolean


    fun setTransient(value: Boolean)
}