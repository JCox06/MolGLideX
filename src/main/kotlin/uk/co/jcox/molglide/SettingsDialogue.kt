package uk.co.jcox.molglide

import java.awt.BorderLayout
import java.awt.GraphicsEnvironment
import javax.swing.*

class SettingsDialogue (mainFrame: JFrame) : JDialog (mainFrame, "Settings", ModalityType.APPLICATION_MODAL) {

    private val modify = AppSettings.settings.copy()

    init {
        val mainPanel = JPanel(BorderLayout())
//        mainPanel.border = BorderFactory.createEmptyBorder(20, 20, 20, 20)
        val label = JLabel("*Some configurations may require application restart to take effect")
        mainPanel.add(label, BorderLayout.PAGE_START)

        val tabbedLayout = JTabbedPane()
        tabbedLayout.addTab("Appearance", buildAppearanceMenu())
        mainPanel.add(tabbedLayout, BorderLayout.CENTER)
        mainPanel.add(buildCloseButtons(), BorderLayout.PAGE_END)

        this.setLocationRelativeTo(mainFrame)
        this.add(mainPanel)
        this.pack()
    }


    private fun buildAppearanceMenu(): JPanel {
        val panel = JPanel()

        val label = JLabel("Select Application Theme")

        val themeArr = AppSettings.themes.keys.toTypedArray()
        val themeSelection = JComboBox(themeArr)
        val currentTheme = AppSettings.settings.lookAndFeel
        val themeID = themeArr.indexOf(currentTheme)
        themeSelection.selectedIndex = themeID
        themeSelection.addActionListener {
            val newTheme = themeSelection.getItemAt(themeSelection.selectedIndex)
            modify.lookAndFeel = newTheme
        }


        val componentAlias = JCheckBox("Component Anti-Aliasing", AppSettings.settings.componentAntialiasing)
        componentAlias.addActionListener { modify.componentAntialiasing = componentAlias.isSelected }

        val textAlias = JCheckBox("Text Anti-Aliasing", AppSettings.settings.textAntialiasing)
        textAlias.addActionListener { modify.textAntialiasing = textAlias.isSelected }

        val fontOptions = GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames

        val fontLabel = JLabel("Select Editor Font")
        val fontSelection = JComboBox(fontOptions)
        val fontID = fontOptions.indexOf(AppSettings.settings.editorFont)
        fontSelection.selectedIndex = fontID
        fontSelection.addActionListener {
            val newFont = fontSelection.getItemAt(fontSelection.selectedIndex)
            modify.editorFont = newFont
        }

        panel.add(label)
        panel.add(themeSelection)
        panel.add(componentAlias)
        panel.add(textAlias)
        panel.add(fontLabel)
        panel.add(fontSelection)

        return panel
    }


    private fun buildCloseButtons(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)

        val close = JButton("Close")
        val apply = JButton("Apply")

        close.addActionListener { dispose() }
        apply.addActionListener {
            AppSettings.settings = modify
            AppSettings.saveToDisc()
            dispose()
        }

        panel.add(close)
        panel.add(apply)

        return panel
    }
}