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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.panashe.bible.BibleData
import org.panashe.bible.SearchIndexEntry
import org.panashe.bible.ui.Accent
import org.panashe.bible.ui.Ink
import org.panashe.bible.ui.Line
import org.panashe.bible.ui.Muted
import org.panashe.bible.ui.components.PanasheDialog

private data class ScoredResult(val entry: SearchIndexEntry, val score: Int)

private fun scoreResult(entry: SearchIndexEntry, query: String): Int {
    val text = entry.text.lowercase()
    val q = query.lowercase()
    var score = 0
    val wordBoundary = Regex("\\b${Regex.escape(q)}\\b")
    if (wordBoundary.containsMatchIn(text)) score += 10
    if (text.contains(q)) score += 5
    if (text.startsWith(q)) score += 3
    val ref = "${entry.book} ${entry.chapter}:${entry.verse}"
    if (ref.lowercase().contains(q)) score += 2
    return score
}

private fun highlightText(text: String, query: String): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(text)
    val q = query.lowercase()
    return buildAnnotatedString {
        var lastIndex = 0
        val lowerText = text.lowercase()
        while (true) {
            val index = lowerText.indexOf(q, lastIndex)
            if (index == -1) {
                append(text.substring(lastIndex))
                break
            }
            if (index > lastIndex) {
                append(text.substring(lastIndex, index))
            }
            withStyle(SpanStyle(background = Accent.copy(alpha = 0.15f), fontWeight = FontWeight.SemiBold)) {
                append(text.substring(index, index + q.length))
            }
            lastIndex = index + q.length
        }
    }
}

@Composable
fun SearchDialog(
    bibleData: BibleData,
    onDismissRequest: () -> Unit,
    onNavigateToVerse: (bookSlug: String, chapter: Int, verse: Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val filters = listOf("All", "Old Testament", "New Testament", "Apocrypha")

    val results = remember(searchQuery, selectedFilter, bibleData) {
        if (searchQuery.length < 3) return@remember emptyList()
        var hits = bibleData.search(searchQuery, limit = 200)
        if (selectedFilter != "All") {
            hits = hits.filter { entry ->
                bibleData.manifest.books.firstOrNull { it.slug == entry.book }?.section == selectedFilter
            }
        }
        hits.map { ScoredResult(it, scoreResult(it, searchQuery)) }
            .filter { it.score > 0 }
            .sortedByDescending { it.score }
            .take(50)
            .map { it.entry }
    }

    PanasheDialog(
        onDismissRequest = onDismissRequest,
        eyebrow = "Search",
        title = "Find verses"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 25.dp).padding(bottom = 25.dp)
        ) {
            Spacer(Modifier.height(20.dp))

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

            if (searchQuery.length >= 3) {
                if (results.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "No verses found for \"$searchQuery\"",
                            color = Muted,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp)) {
                        items(results) { entry ->
                            val bookName = bibleData.manifest.bookName(entry.book) ?: entry.book
                            val reference = "$bookName ${entry.chapter}:${entry.verse}"
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onNavigateToVerse(entry.book, entry.chapter, entry.verse)
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
                                    highlightText(entry.text, searchQuery),
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
                Box(modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Search the Bible", fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text("Type at least 3 characters to find verses.", color = Muted, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
