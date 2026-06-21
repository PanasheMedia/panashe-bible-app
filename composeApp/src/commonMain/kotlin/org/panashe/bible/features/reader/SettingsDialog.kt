package org.panashe.bible.features.reader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.panashe.bible.ui.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    prefs: MutableReaderPreferences,
    onDismissRequest: () -> Unit
) {
    val snapshot = prefs.snapshot()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp)
        ) {
            Text(
                "Reading preferences",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Reading settings",
                color = Ink,
                fontFamily = FontFamily.Serif,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(24.dp))

            // Verse Numbers Toggle
            SettingToggleRow(
                title = "Verse numbers",
                description = "Show verse references alongside Scripture.",
                checked = snapshot.showVerseNumbers,
                onCheckedChange = { prefs.toggleVerseNumbers() }
            )

            HorizontalDivider(color = Line)

            // Line by line Toggle
            SettingToggleRow(
                title = "Line by line",
                description = "Place each verse on a separate line for slower reading.",
                checked = snapshot.lineByLine,
                onCheckedChange = { prefs.toggleLineByLine() }
            )

            HorizontalDivider(color = Line)

            // Text Size
            SettingRowHeader("Text size", "Adjust the size of Scripture text.")
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth()
                    .border(1.dp, Line, RoundedCornerShape(4.dp))
                    .height(62.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight()
                        .background(Soft).clickable { prefs.decreaseTextSize() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("-", fontSize = 24.sp)
                }
                Box(
                    modifier = Modifier.weight(2f).fillMaxHeight()
                        .border(BorderStroke(1.dp, Line)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${snapshot.textSizePercent.toInt()}%",
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                }
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight()
                        .background(Soft).clickable { prefs.increaseTextSize() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", fontSize = 24.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = Line)
            Spacer(modifier = Modifier.height(16.dp))

            SettingRowHeader("Reading font", "Choose the typeface used for Scripture.")
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FontOption("Source Serif", "Balanced", snapshot.fontLabel == "Source Serif") {
                    prefs.setFont("Source Serif", FontFamily.Serif)
                }
                FontOption("DM Sans", "Clean", snapshot.fontLabel == "DM Sans") {
                    prefs.setFont("DM Sans", FontFamily.SansSerif)
                }
                FontOption("Mono", "Technical", snapshot.fontLabel == "Mono") {
                    prefs.setFont("Mono", FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun SettingToggleRow(title: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(2.dp))
            Text(description, color = Muted, fontSize = 12.sp, lineHeight = 16.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.surface,
                checkedTrackColor = MaterialTheme.colorScheme.secondary,
                uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun SettingRowHeader(title: String, description: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Text(title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(Modifier.height(2.dp))
        Text(description, color = Muted, fontSize = 12.sp, lineHeight = 16.sp)
    }
}

@Composable
fun FontOption(name: String, description: String, selected: Boolean, onClick: () -> Unit) {
    val borderColor = if (selected) Ink else Line
    val bgColor = if (selected) Soft else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .background(bgColor, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Ag",
            fontSize = 24.sp,
            modifier = Modifier.width(48.dp),
            color = Ink
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(description, color = Muted, fontSize = 11.sp)
        }

        Box(
            modifier = Modifier
                .size(16.dp)
                .border(
                    if (selected) 4.dp else 1.dp,
                    if (selected) Accent else Muted,
                    RoundedCornerShape(8.dp)
                )
        )
    }
}
