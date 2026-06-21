package org.panashe.bible.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

import org.jetbrains.compose.resources.Font
import panashebible.composeapp.generated.resources.Res
import panashebible.composeapp.generated.resources.*

val Ink = Color(0xFF262624)
val Muted = Color(0xFF77756F)
val Paper = Color(0xFFF8F7F3)
val SurfaceColor = Color(0xFFFFFFFF)
val Soft = Color(0xFFEFECE5)
val Line = Color(0xFFDFDDD6)
val Accent = Color(0xFFC64F35)

val InkDark = Color(0xFFEEEAE2)
val MutedDark = Color(0xFFAAA69D)
val PaperDark = Color(0xFF181817)
val SurfaceColorDark = Color(0xFF222220)
val SoftDark = Color(0xFF2C2A27)
val LineDark = Color(0xFF3D3B37)
val AccentDark = Color(0xFFE58067)

@Composable
fun getSansFontFamily() = FontFamily(
    Font(Res.font.DMSans_Regular, FontWeight.Normal),
    Font(Res.font.DMSans_Bold, FontWeight.Bold)
)

@Composable
fun getSerifFontFamily() = FontFamily(
    Font(Res.font.SourceSerif4_Variable)
)

@Composable
fun getLexendFontFamily() = FontFamily(
    Font(Res.font.Lexend_Variable)
)

@Composable
fun getBaskervilleFontFamily() = FontFamily(
    Font(Res.font.LibreBaskerville_Regular)
)

@Composable
fun getAtkinsonFontFamily() = FontFamily(
    Font(Res.font.AtkinsonHyperlegible_Regular)
)

@Composable
fun PanasheTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val sans = getSansFontFamily()
    val serif = getSerifFontFamily()
    
    val ink = if (darkTheme) InkDark else Ink
    val paper = if (darkTheme) PaperDark else Paper
    val accent = if (darkTheme) AccentDark else Accent
    val muted = if (darkTheme) MutedDark else Muted
    val line = if (darkTheme) LineDark else Line
    val soft = if (darkTheme) SoftDark else Soft
    val surface = if (darkTheme) SurfaceColorDark else SurfaceColor

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = ink,
            secondary = accent,
            surface = paper,
            onSurface = ink
        ),
        typography = MaterialTheme.typography.copy(
            displayLarge = MaterialTheme.typography.displayLarge.copy(fontFamily = serif),
            displayMedium = MaterialTheme.typography.displayMedium.copy(fontFamily = serif),
            headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontFamily = serif),
            titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = serif),
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = sans),
            bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontFamily = sans),
            bodySmall = MaterialTheme.typography.bodySmall.copy(fontFamily = sans),
            labelLarge = MaterialTheme.typography.labelLarge.copy(fontFamily = sans),
            labelMedium = MaterialTheme.typography.labelMedium.copy(fontFamily = sans),
            labelSmall = MaterialTheme.typography.labelSmall.copy(fontFamily = sans)
        ),
        content = content
    )
}

