package uk.co.jcox.molglide.editor.model.chemengine


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
