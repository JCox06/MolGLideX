package uk.co.jcox.molglide

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecodingException
import java.io.File
import javax.swing.UIManager

object AppSettings {
    private val SETTINGS_FILE = File(MolGLideUtils.getMolGLideHome(), "settings.json")

    val themes = mutableMapOf(
        "FlatLaf Light" to "com.formdev.flatlaf.FlatLightLaf",
        "FlatLaf Dark" to "com.formdev.flatlaf.FlatDarkLaf",
        "Java Metal" to "javax.swing.plaf.metal.MetalLookAndFeel",
        "Motif" to "com.sun.java.swing.plaf.motif.MotifLookAndFeel",
        "System Theme" to UIManager.getCrossPlatformLookAndFeelClassName(),
    )


    init {
        if (!SETTINGS_FILE.exists()) {
            SETTINGS_FILE.createNewFile()
        }
    }

    //Init with default values to begin with
    var settings = SettingsData()

    fun refreshDataFromDisc() {
        try {
            settings = Json.decodeFromString<SettingsData>(SETTINGS_FILE.readText())
        } catch (e: JsonDecodingException) {
            settings = SettingsData()
        }

    }

    fun saveToDisc() {
        val json = Json.encodeToString(settings)
        SETTINGS_FILE.writeText(json)
    }
}

@Serializable
data class SettingsData (
    var lookAndFeel: String = AppSettings.themes.keys.first(),
    var componentAntialiasing: Boolean = true,
    var textAntialiasing: Boolean = true,
    var editorFont: String = "Liberation Serif"
)