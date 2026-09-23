package uk.co.jcox.molglide

import java.awt.BorderLayout
import java.awt.GraphicsEnvironment
import javax.swing.*

class SettingsDialogue (mainFrame: JFrame) : JDialog (mainFrame, "Settings", ModalityType.APPLICATION_MODAL) {

    private val modify = AppConfig.settings.copy()

    init {
        val mainPanel = JPanel(BorderLayout())
//        mainPanel.border = BorderFactory.createEmptyBorder(20, 20, 20, 20)
        val label = JLabel("*Some configurations may require application restart to take effect")
        mainPanel.add(label, BorderLayout.PAGE_START)

        val tabbedLayout = JTabbedPane()
        tabbedLayout.addTab("Appearance", buildAppearanceMenu())
        tabbedLayout.addTab("Development", buildDevelopmentMenu())
        mainPanel.add(tabbedLayout, BorderLayout.CENTER)
        mainPanel.add(buildCloseButtons(), BorderLayout.PAGE_END)

        this.add(mainPanel)
        this.pack()
        this.setLocationRelativeTo(mainFrame)
    }


    private fun buildAppearanceMenu(): JPanel {
        val panel = JPanel()

        val label = JLabel("Select Application Theme")

        val themeArr = AppConfig.themes.keys.toTypedArray()
        val themeSelection = JComboBox(themeArr)
        val currentTheme = AppConfig.settings.lookAndFeel
        val themeID = themeArr.indexOf(currentTheme)
        themeSelection.selectedIndex = themeID
        themeSelection.addActionListener {
            val newTheme = themeSelection.getItemAt(themeSelection.selectedIndex)
            modify.lookAndFeel = newTheme
        }


        val componentAlias = JCheckBox("Component Anti-Aliasing", AppConfig.settings.componentAntialiasing)
        componentAlias.addActionListener { modify.componentAntialiasing = componentAlias.isSelected }

        val textAlias = JCheckBox("Text Anti-Aliasing", AppConfig.settings.textAntialiasing)
        textAlias.addActionListener { modify.textAntialiasing = textAlias.isSelected }

        val fontOptions = GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames

        val fontLabel = JLabel("Select Editor Font")
        val fontSelection = JComboBox(fontOptions)
        val fontID = fontOptions.indexOf(AppConfig.settings.editorFont)
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

    private fun buildDevelopmentMenu(): JPanel {
        val panel = JPanel()

        val checkbox = JCheckBox("Launch in Debug Mode", AppConfig.settings.debugMode)
        checkbox.addActionListener { modify.debugMode = checkbox.isSelected }

        panel.add(checkbox)
        return panel
    }


    private fun buildCloseButtons(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)

        val close = JButton("Close")
        val apply = JButton("Apply")

        close.addActionListener { dispose() }
        apply.addActionListener {
            AppConfig.settings = modify
            AppConfig.saveToDisc()
            dispose()
        }

        panel.add(close)
        panel.add(apply)

        return panel
    }
}