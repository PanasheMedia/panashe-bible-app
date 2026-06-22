package org.panashe.bible.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Vector icons mirroring the web's stroke SVGs (styles.css: stroke-width 1.7, round caps).
 * `Icon(...)` applies a tint ColorFilter, so the placeholder black stroke/fill is recolored
 * at render time.
 */
private fun strokeIcon(name: String, pathData: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        addPath(
            pathData = addPathNodes(pathData),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.7f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
    }.build()

private fun filledIcon(name: String, pathData: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        addPath(pathData = addPathNodes(pathData), fill = SolidColor(Color.Black))
    }.build()

// Web: <path d="M20.5 15.2A8.5 8.5 0 0 1 8.8 3.5a8.5 8.5 0 1 0 11.7 11.7Z"/>
val MoonIcon: ImageVector by lazy {
    strokeIcon("Moon", "M20.5 15.2A8.5 8.5 0 0 1 8.8 3.5a8.5 8.5 0 1 0 11.7 11.7Z")
}

// Sun: circle r4 + eight rays (shown when already in dark mode).
val SunIcon: ImageVector by lazy {
    strokeIcon(
        "Sun",
        "M12 8 A4 4 0 1 1 11.99 8 Z " +
            "M12 2 V4 M12 20 V22 M2 12 H4 M20 12 H22 " +
            "M4.9 4.9 6.3 6.3 M17.7 17.7 19.1 19.1 M19.1 4.9 17.7 6.3 M6.3 17.7 4.9 19.1"
    )
}

// Web: <circle cx=11 cy=11 r=8/><path d="m21 21-4.3-4.3"/>
val SearchIcon: ImageVector by lazy {
    strokeIcon("Search", "M19 11 A8 8 0 1 1 18.99 11 Z M21 21 16.7 16.7")
}

// Web: <path d="m6 6 12 12M18 6 6 18"/>
val CloseIcon: ImageVector by lazy {
    strokeIcon("Close", "M6 6 18 18 M18 6 6 18")
}

// Web: <path d="M7 4v16l13-8z" fill/>
val PlayIcon: ImageVector by lazy {
    filledIcon("Play", "M7 4 V20 L20 12 Z")
}
