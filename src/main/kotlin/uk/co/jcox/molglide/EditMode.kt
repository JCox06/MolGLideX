package uk.co.jcox.molglide

enum class EditMode (val symbol: String, val type: ToolType) {
    INSERT_CARBON("C", ToolType.ATOM_INSERT),
    INSERT_HYDROGEN("H", ToolType.ATOM_INSERT),
    INSERT_OXYGEN("O", ToolType.ATOM_INSERT),
    INSERT_NITROGEN("N", ToolType.ATOM_INSERT),
    INSERT_PHOSPHORUS("P", ToolType.ATOM_INSERT),
    INSERT_FLUORINE("F",ToolType.ATOM_INSERT ),
    INSERT_CHLORINE("Cl", ToolType.ATOM_INSERT),
    INSERT_BROMINE("Br", ToolType.ATOM_INSERT),
    INSERT_IODINE("I", ToolType.ATOM_INSERT),
    INSERT_MAGNESIUM("Mg", ToolType.ATOM_INSERT),
    INSERT_LITHIUM("Li", ToolType.ATOM_INSERT),
    INSERT_SULPHUR("S", ToolType.ATOM_INSERT),
    INSERT_WILD_CARD("R", ToolType.ATOM_INSERT),
;

    enum class ToolType {
        ATOM_INSERT,
        RING_INSERT
    }
}

