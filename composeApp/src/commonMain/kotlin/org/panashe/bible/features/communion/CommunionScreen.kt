package org.panashe.bible.features.communion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.panashe.bible.BibleData
import org.panashe.bible.formatDate
import org.panashe.bible.ui.components.Eyebrow
import org.panashe.bible.ui.components.LoadingText
import org.panashe.bible.ui.components.PanasheDialog
import org.panashe.bible.ui.components.SectionCard
import org.panashe.bible.ui.components.StaggeredEntrance

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommunionScreen(
    view: CommunionView?,
    bibleData: BibleData?,
    onReadChapter: (bookSlug: String, chapter: Int) -> Unit = { _, _ -> },
) {
    var showArchiveDialog by remember { mutableStateOf(false) }
    var selectedArchiveIso by remember { mutableStateOf<String?>(null) }

    val reading = view?.reading
    // One generator for the archive list; only depends on the bundled data.
    val generator = remember(bibleData) {
        bibleData?.let { CommunionGenerator(it.manifest, it.seed) }
    }

    // Today's Communion is the witness gathered live by the server (same for all
    // readers, counts hidden), carried on the loaded view.
    val kept = view?.kept

    // Archive: the last seven days of gathered Communions.
    val archiveEntries = remember(bibleData) {
        val data = bibleData ?: return@remember emptyList()
        val gen = generator!!
        val today = CommunionGenerator.todayIso()
        (1..7).mapNotNull { daysAgo ->
            val iso = gen.isoForDate(today, -daysAgo) ?: return@mapNotNull null
            val day = gen.communionForDate(iso)
            ArchiveDay(
                iso = iso,
                dateLabel = formatDate(iso),
                reference = data.displayReference(day.gathered),
                offerings = day.offerings
            )
        }
    }

    val archiveDetailResult by produceState<ArchiveDetail?>(initialValue = null, selectedArchiveIso, bibleData) {
        val data = bibleData
        val iso = selectedArchiveIso
        val gen = generator
        if (data == null || iso == null || gen == null) {
            value = null
            return@produceState
        }
        val day = gen.communionForDate(iso)
        value = ArchiveDetail(
            dateLabel = formatDate(iso),
            gatheredRef = data.displayReference(day.gathered),
            gatheredText = data.passageTextWithVerseNumbers(day.gathered),
            offerings = day.offerings.map { ref ->
                ArchiveDetailEntry(data.displayReference(ref), data.passageTextWithVerseNumbers(ref))
            }
        )
    }
    val archiveDetail = archiveDetailResult

    CommunionHero()

    TabCard {
        Eyebrow("Today's Communion · ${kept?.date ?: ""}".trimEnd(' ', '·'))
        Spacer(Modifier.height(8.dp))
        Text(
            "The Scripture readers are bringing today, gathered around the Word.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            lineHeight = 21.sp
        )
        Spacer(Modifier.height(18.dp))
        if (kept == null) {
            LoadingText("Gathering today's Communion...")
        } else {
            KeptThread(kept, onReadChapter)
        }
    }

    // Archive card with responsive grid layout
    SectionCard {
        CardHeading("Past Communion")
        Spacer(Modifier.height(14.dp))
        if (archiveEntries.isNotEmpty() || kept != null) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Today's entry
                if (kept != null) {
                    ArchiveItem(
                        date = kept.date,
                        reference = reading?.display ?: "",
                        isToday = true,
                        entranceDelayMillis = 240,
                        modifier = Modifier.widthIn(min = 190.dp, max = 380.dp).weight(1f)
                    )
                }
                // Previous days
                archiveEntries.forEachIndexed { index, entry ->
                    ArchiveItem(
                        date = entry.dateLabel,
                        reference = entry.reference,
                        isToday = false,
                        entranceDelayMillis = 300 + index * 50,
                        modifier = Modifier.widthIn(min = 190.dp, max = 380.dp).weight(1f)
                            .clickable {
                                selectedArchiveIso = entry.iso
                                showArchiveDialog = true
                            }
                    )
                }
            }
        }
    }

    // Archive detail dialog
    if (showArchiveDialog && archiveDetail != null) {
        ArchiveDetailDialog(
            detail = archiveDetail!!,
            onDismiss = { showArchiveDialog = false }
        )
    }

}

/** Serif section heading shared across the Communion cards (web .daily-card h2 / .communion-section-title). */
@Composable
private fun CardHeading(text: String) {
    Text(
        text,
        color = MaterialTheme.colorScheme.onSurface,
        fontFamily = FontFamily.Serif,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 28.sp
    )
}

// --- Archive Detail Dialog ---

@Composable
fun ArchiveDetailDialog(detail: ArchiveDetail, onDismiss: () -> Unit) {
    PanasheDialog(
        onDismissRequest = onDismiss,
        eyebrow = "Past Communion",
        title = detail.dateLabel
    ) {
        LazyColumn(modifier = Modifier.padding(25.dp)) {
            // Gathered passage reference
            item {
                Text(
                    detail.gatheredRef,
                    color = MaterialTheme.colorScheme.secondary,
                    fontFamily = FontFamily.Serif,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 28.sp
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    detail.gatheredText,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Serif,
                    fontSize = 16.sp,
                    lineHeight = 26.sp
                )
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.height(12.dp))
                Text(
                    "RELATED CHAPTERS",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp
                )
                Spacer(Modifier.height(8.dp))
            }
            // Offerings
            items(detail.offerings) { offering ->
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Text(
                        offering.reference,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        offering.text,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Serif,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CommunionHero() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Daily Communion",
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Serif,
            fontSize = 45.sp,
            lineHeight = 46.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-2).sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(14.dp))
        Text(
            "Offer Communion, and see what gathers around the Word today.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 15.sp,
            lineHeight = 25.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 560.dp)
        )
    }
}

// --- Communion thread: the gathered passage + the verses kept beneath it ---

@Composable
fun KeptThread(communion: KeptCommunion, onReadChapter: (String, Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        GatheredPassage(communion.gathered, onReadChapter)
        if (communion.beneath.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                communion.beneath.forEach { entry -> KeptOffering(entry, onReadChapter) }
            }
        }
    }
}

/** The most-resonant passage today, set apart with an accent border (the thread's head). */
@Composable
private fun GatheredPassage(entry: CommunionEntry, onReadChapter: (String, Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f), RoundedCornerShape(4.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                entry.display,
                color = MaterialTheme.colorScheme.secondary,
                fontFamily = FontFamily.Serif,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.weight(1f))
            ReadChapterLink(entry.reference.book, entry.reference.chapter, onReadChapter)
        }
        if (entry.preview.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(
                entry.preview,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Serif,
                fontSize = 15.sp,
                lineHeight = 26.sp
            )
        }
    }
}

/** A verse kept beneath the gathered passage. */
@Composable
private fun KeptOffering(entry: CommunionEntry, onReadChapter: (String, Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                entry.display,
                color = MaterialTheme.colorScheme.secondary,
                fontFamily = FontFamily.Serif,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.weight(1f))
            ReadChapterLink(entry.reference.book, entry.reference.chapter, onReadChapter)
        }
        if (entry.preview.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                entry.preview,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 22.sp
            )
        }
    }
}

/** Tappable "Read Chapter" link used across the witness sections. */
@Composable
private fun ReadChapterLink(bookSlug: String, chapter: Int, onReadChapter: (String, Int) -> Unit) {
    Text(
        "Read Chapter",
        color = MaterialTheme.colorScheme.secondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable { onReadChapter(bookSlug, chapter) }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

// --- Joined tab card (web .communion-tab-card) ---

@Composable
fun TabCard(content: @Composable ColumnScope.() -> Unit) {
    StaggeredEntrance(delayMillis = 140) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 26.dp),
                content = content
            )
        }
    }
}

// --- Archive grid item (matches web .communion-archive-item) ---

@Composable
fun ArchiveItem(
    date: String,
    reference: String,
    isToday: Boolean,
    entranceDelayMillis: Int = 0,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    StaggeredEntrance(delayMillis = entranceDelayMillis) {
        Surface(
            color = if (isHovered) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f) else MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                1.dp,
                if (isToday || isHovered) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
            ),
            shape = RoundedCornerShape(4.dp),
            modifier = modifier.hoverable(interactionSource)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(date, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, lineHeight = 14.sp)
                    if (isToday) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "TODAY",
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.4.sp,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(3.dp))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    reference,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Serif,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Settled",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
