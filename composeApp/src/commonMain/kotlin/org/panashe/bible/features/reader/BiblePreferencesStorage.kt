package org.panashe.bible.features.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontFamily

/**
 * Local reader preferences for font, size, verse numbers, line-by-line.
 */
data class ReaderPreferences(
    val fontFamily: FontFamily = FontFamily.Serif,
    val fontLabel: String = "Source Serif",
    val textSizePercent: Float = 100f,
    val showVerseNumbers: Boolean = true,
    val lineByLine: Boolean = false
) {
    val textSizeMultiplier: Float get() = textSizePercent / 100f
    val baseFontSizeSp: Float get() = 18f * textSizeMultiplier
    val lineHeightSp: Float get() = 33f * textSizeMultiplier
}

val LocalReaderPreferences = compositionLocalOf { ReaderPreferences() }

@Composable
fun rememberReaderPreferences(): ReaderPreferences {
    var fontFamily by remember { mutableStateOf(FontFamily.Serif) }
    var fontLabel by remember { mutableStateOf("Source Serif") }
    var textSizePercent by remember { mutableFloatStateOf(100f) }
    var showVerseNumbers by remember { mutableStateOf(true) }
    var lineByLine by remember { mutableStateOf(false) }

    return ReaderPreferences(
        fontFamily = fontFamily,
        fontLabel = fontLabel,
        textSizePercent = textSizePercent,
        showVerseNumbers = showVerseNumbers,
        lineByLine = lineByLine
    ).also { prefs ->
        // Expose mutation lambdas via a wrapper pattern
    }
}

/**
 * Mutable holder for reader preferences, passed down to SettingsDialog and BibleScreen.
 */
class MutableReaderPreferences(
    initial: ReaderPreferences = ReaderPreferences()
) {
    var fontFamily by mutableStateOf(initial.fontFamily)
    var fontLabel by mutableStateOf(initial.fontLabel)
    var textSizePercent by mutableFloatStateOf(initial.textSizePercent)
    var showVerseNumbers by mutableStateOf(initial.showVerseNumbers)
    var lineByLine by mutableStateOf(initial.lineByLine)

    fun snapshot() = ReaderPreferences(
        fontFamily = fontFamily,
        fontLabel = fontLabel,
        textSizePercent = textSizePercent,
        showVerseNumbers = showVerseNumbers,
        lineByLine = lineByLine
    )

    fun increaseTextSize() { textSizePercent = (textSizePercent + 10f).coerceAtMost(200f) }
    fun decreaseTextSize() { textSizePercent = (textSizePercent - 10f).coerceAtMost(50f) }

    fun setFont(label: String, family: FontFamily) {
        fontLabel = label
        fontFamily = family
    }

    fun toggleVerseNumbers() { showVerseNumbers = !showVerseNumbers }
    fun toggleLineByLine() { lineByLine = !lineByLine }
}
