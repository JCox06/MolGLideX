package uk.co.jcox.molglide.ui

import java.awt.event.ActionEvent
import javax.swing.AbstractAction

fun abstractAction(name: String, func: (ActionEvent?) -> Unit): AbstractAction {
    return object : AbstractAction(name) {
        override fun actionPerformed(e: ActionEvent?) {
            func(e)
        }
    }
}