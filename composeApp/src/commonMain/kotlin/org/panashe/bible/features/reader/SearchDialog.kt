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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.panashe.bible.BibleData
import org.panashe.bible.BookSummary
import org.panashe.bible.SearchIndexEntry
import org.panashe.bible.ui.Ink
import org.panashe.bible.ui.Line
import org.panashe.bible.ui.Muted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDialog(
    bibleData: BibleData,
    onDismissRequest: () -> Unit,
    onNavigateToVerse: (bookSlug: String, chapter: Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val filters = listOf("All", "Old Testament", "New Testament")

    val results = remember(searchQuery, selectedFilter, bibleData) {
        if (searchQuery.isBlank()) return@remember emptyList()
        var hits = bibleData.search(searchQuery, limit = 100)
        if (selectedFilter != "All") {
            val section = if (selectedFilter == "Old Testament") "Old Testament" else "New Testament"
            hits = hits.filter { entry ->
                bibleData.manifest.books.firstOrNull { it.slug == entry.book }?.section == section
            }
        }
        hits
    }

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
                "SEARCH",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Find verses",
                color = Ink,
                fontFamily = FontFamily.Serif,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
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
                if (results.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            "No verses found for \"$searchQuery\"",
                            color = Muted,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        items(results) { entry ->
                            val bookName = bibleData.manifest.bookName(entry.book) ?: entry.book
                            val reference = "$bookName ${entry.chapter}:${entry.verse}"
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onNavigateToVerse(entry.book, entry.chapter)
                                        onDismissRequest()
                                    }
                                    .padding(vertical = 16.dp)
                            ) {
                                Text(
                                    reference,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.1.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    entry.text,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp,
                                    color = Ink
                                )
                            }
                            HorizontalDivider(color = Line)
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Search the Bible", fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text("Type keywords to find verses.", color = Muted, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
