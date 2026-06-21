package org.panashe.bible.features.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.panashe.bible.ui.getSerifFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDialog(
    onDismissRequest: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    
    val filters = listOf("All", "Old Testament", "New Testament", "Apocrypha")
    
    // Mock results
    val results = listOf(
        "John 1:1" to "In the beginning was the Word, and the Word was with God, and the Word was God.",
        "John 1:2" to "The same was in the beginning with God."
    )

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
                "Search",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Find verses",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = getSerifFontFamily()
            )
            Spacer(Modifier.height(24.dp))
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search the Scriptures...") },
                shape = RoundedCornerShape(6.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                ),
                singleLine = true
            )
            
            Spacer(Modifier.height(14.dp))
            
            // Filters row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                filters.forEach { filter ->
                    val isSelected = filter == selectedFilter
                    val bgColor = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent
                    val contentColor = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface
                    val borderColor = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    
                    Box(
                        modifier = Modifier
                            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
                            .background(bgColor, RoundedCornerShape(4.dp))
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(filter, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = contentColor)
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            if (searchQuery.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    items(results) { (reference, text) ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { }
                                .padding(vertical = 16.dp)
                        ) {
                            Text(reference, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.1.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(text, fontFamily = getSerifFontFamily(), fontSize = 16.sp, lineHeight = 24.sp)
                        }
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Text("Search the Bible", fontFamily = getSerifFontFamily(), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text("Type keywords to find verses.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
