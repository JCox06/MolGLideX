package uk.co.jcox.molglide

enum class EditMode (val symbol: String, val ringSize: Int, val type: ToolType) {
    INSERT_CARBON("C", 0, ToolType.ATOM_INSERT),
    INSERT_HYDROGEN("H", 0, ToolType.ATOM_INSERT),
    INSERT_OXYGEN("O", 0,  ToolType.ATOM_INSERT),
    INSERT_NITROGEN("N", 0,  ToolType.ATOM_INSERT),
    INSERT_PHOSPHORUS("P", 0,  ToolType.ATOM_INSERT),
    INSERT_FLUORINE("F", 0, ToolType.ATOM_INSERT ),
    INSERT_CHLORINE("Cl", 0,  ToolType.ATOM_INSERT),
    INSERT_BROMINE("Br", 0,  ToolType.ATOM_INSERT),
    INSERT_IODINE("I", 0,  ToolType.ATOM_INSERT),
    INSERT_MAGNESIUM("Mg", 0,  ToolType.ATOM_INSERT),
    INSERT_LITHIUM("Li", 0,  ToolType.ATOM_INSERT),
    INSERT_SULPHUR("S", 0,  ToolType.ATOM_INSERT),
    INSERT_SILICON("Si", 0,  ToolType.ATOM_INSERT),
    INSERT_OSMIUM("Os", 0,  ToolType.ATOM_INSERT),
    INSERT_WILD_CARD("R", 0,  ToolType.ATOM_INSERT),


    RING_BENZENE("Benzene", 6, ToolType.RING_INSERT),
    RING_CYCLOHEXANE("Cyclohexane", 6, ToolType.RING_INSERT),
    RING_CYCLOPENTANE("Cyclopentane", 5, ToolType.RING_INSERT),
    RING_CYCLOOCTANE("Cyclooctane", 8, ToolType.RING_INSERT),

;

    enum class ToolType {
        ATOM_INSERT,
        RING_INSERT
    }
}

