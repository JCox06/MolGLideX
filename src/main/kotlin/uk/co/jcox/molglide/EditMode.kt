package uk.co.jcox.molglide

import com.formdev.flatlaf.extras.FlatSVGIcon

enum class EditMode (val symbol: String, val ringSize: Int, val type: ToolType, val icon: FlatSVGIcon? = null) {

    GENERIC_SELECT("Select", 0, ToolType.SELECT_TOOL, FlatSVGIcon("uk/co/jcox/molglide/select.svg", javaClass.classLoader)),

    INSERT_CARBON("C", 0, ToolType.ATOM_INSERT),
    INSERT_HYDROGEN("H", 0, ToolType.ATOM_INSERT),
    INSERT_OXYGEN("O", 0,  ToolType.ATOM_INSERT),
    INSERT_NITROGEN("N", 0,  ToolType.ATOM_INSERT),
    INSERT_PHOSPHORUS("P", 0,  ToolType.ATOM_INSERT),
    INSERT_FLUORINE("F", 0, ToolType.ATOM_INSERT ),
    INSERT_CHLORINE("Cl", 0,  ToolType.ATOM_INSERT),
    INSERT_BROMINE("Br", 0,  ToolType.ATOM_INSERT),
    INSERT_IODINE("I", 0,  ToolType.ATOM_INSERT),
//    INSERT_MAGNESIUM("Mg", 0,  ToolType.ATOM_INSERT),
//    INSERT_LITHIUM("Li", 0,  ToolType.ATOM_INSERT),
    INSERT_SULPHUR("S", 0,  ToolType.ATOM_INSERT),
    INSERT_SILICON("Si", 0,  ToolType.ATOM_INSERT),
//    INSERT_OSMIUM("Os", 0,  ToolType.ATOM_INSERT),
    INSERT_WILD_CARD("R", 0,  ToolType.ATOM_INSERT),


    RING_BENZENE("Benzene", 6, ToolType.RING_INSERT, FlatSVGIcon("uk/co/jcox/molglide/benzene.svg", javaClass.classLoader)),
    RING_CYCLOHEXANE("Cyclohexane", 6, ToolType.RING_INSERT, FlatSVGIcon("uk/co/jcox/molglide/cyclohexane.svg", javaClass.classLoader)),
    RING_CYCLOPENTANE("Cyclopentane", 5, ToolType.RING_INSERT, FlatSVGIcon("uk/co/jcox/molglide/cyclopentane.svg", javaClass.classLoader)),
    RING_CYCLOOCTANE("Cyclooctane", 8, ToolType.RING_INSERT, FlatSVGIcon("uk/co/jcox/molglide/cyclooctane.svg", javaClass.classLoader)),

    LONE_PAIR("Lone Pair", 0,ToolType.FORMAL_CHARGE, FlatSVGIcon("uk/co/jcox/molglide/lone_pair.svg", javaClass.classLoader)),
    CHARGE_PLUS("+", 0,ToolType.FORMAL_CHARGE),
    CHARGE_NEGATIVE("-", 0,ToolType.FORMAL_CHARGE),

    ;

    enum class ToolType {
        ATOM_INSERT,
        RING_INSERT,
        SELECT_TOOL,
        FORMAL_CHARGE,
    }
}

