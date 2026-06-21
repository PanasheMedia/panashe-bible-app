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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.panashe.bible.ui.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    onDismissRequest: () -> Unit
) {
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
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = getSerifFontFamily()
            )
            Spacer(Modifier.height(24.dp))
            
            // Verse Numbers Toggle
            SettingToggleRow(
                title = "Verse numbers",
                description = "Show verse references alongside Scripture.",
                checked = true,
                onCheckedChange = {}
            )
            
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            
            // Line by line Toggle
            SettingToggleRow(
                title = "Line by line",
                description = "Place each verse on a separate line for slower reading.",
                checked = false,
                onCheckedChange = {}
            )

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            
            // Text Size
            SettingRowHeader("Text size", "Adjust the size of Scripture text.")
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).height(62.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)).clickable { }, contentAlignment = Alignment.Center) {
                    Text("-", fontSize = 24.sp)
                }
                Box(modifier = Modifier.weight(2f).fillMaxHeight().border(BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))), contentAlignment = Alignment.Center) {
                    Text("100%", fontWeight = FontWeight.Bold, fontFamily = getSansFontFamily())
                }
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)).clickable { }, contentAlignment = Alignment.Center) {
                    Text("+", fontSize = 24.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Audio settings mock
            SettingRowHeader("Audio Voice", "Choose narrator.")
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text("Default Voice")
            }
            Spacer(modifier = Modifier.height(16.dp))
            SettingRowHeader("Audio Speed", "Adjust pace.")
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SpeedButton(".8x", false)
                SpeedButton("1x", true)
                SpeedButton("1.2x", false)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingRowHeader("Reading font", "Choose the typeface used for Scripture.")
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FontOption("Source Serif", "Balanced", true)
                FontOption("Lexend", "Dyslexia-Friendly", false)
                FontOption("Libre Baskerville", "Traditional", false)
                FontOption("Atkinson Hyperlegible", "Accessible", false)
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
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(2.dp))
            Text(description, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp, lineHeight = 16.sp)
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
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(Modifier.height(2.dp))
        Text(description, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp, lineHeight = 16.sp)
    }
}

@Composable
fun SpeedButton(label: String, selected: Boolean) {
    val borderColor = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val bgColor = if (selected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f) else Color.Transparent
    
    Box(
        modifier = Modifier
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .background(bgColor, RoundedCornerShape(4.dp))
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 14.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun FontOption(name: String, description: String, selected: Boolean) {
    val borderColor = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val bgColor = if (selected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f) else Color.Transparent
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .background(bgColor, RoundedCornerShape(4.dp))
            .clickable { }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Ag", fontSize = 24.sp, modifier = Modifier.width(48.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(description, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 11.sp)
        }
        
        // Radio circle mock
        Box(
            modifier = Modifier
                .size(16.dp)
                .border(
                    if (selected) 4.dp else 1.dp,
                    if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    RoundedCornerShape(8.dp)
                )
        )
    }
}
