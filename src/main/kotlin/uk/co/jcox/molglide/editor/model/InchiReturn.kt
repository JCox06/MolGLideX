package uk.co.jcox.molglide.editor.model


enum class InchiStats {
    SUCCESS,
    WARNING,
    ERROR,
}

data class InchiReturn(
    val inchiString: String,
    val inchiStats: InchiStats,
    val log: String,
    )
