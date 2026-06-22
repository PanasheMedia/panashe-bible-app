package org.panashe.bible.features.communion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import org.panashe.bible.ui.components.PrimaryAction
import org.panashe.bible.ui.components.SecondaryAction
import org.panashe.bible.ui.components.SectionCard
import org.panashe.bible.ui.components.StaggeredEntrance
import org.panashe.bible.ui.components.ToastBar
import org.panashe.bible.platform.AppSettings
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommunionScreen(
    view: CommunionView?,
    bibleData: BibleData?,
    appSettings: AppSettings? = null,
    onOffer: (bookSlug: String, chapter: Int, start: Int, end: Int) -> Unit = { _, _, _, _ -> },
    onReadChapter: (bookSlug: String, chapter: Int) -> Unit = { _, _ -> },
) {
    // Check if user already offered today
    val persistedState = remember { appSettings?.load() }
    val todayIso = remember {
        val localDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        "${localDate.year}-${localDate.monthNumber.toString().padStart(2, '0')}-${localDate.dayOfMonth.toString().padStart(2, '0')}"
    }
    var hasOffered by remember { mutableStateOf(persistedState?.offeredTodayIso == todayIso) }
    var showOfferForm by remember { mutableStateOf(false) }
    var showArchiveDialog by remember { mutableStateOf(false) }
    var selectedArchiveIso by remember { mutableStateOf<String?>(null) }
    var showOfferToast by remember { mutableStateOf(false) }

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
            gatheredText = data.passageText(day.gathered),
            offerings = day.offerings.map { ref ->
                ArchiveDetailEntry(data.displayReference(ref), data.passageText(ref))
            }
        )
    }
    val archiveDetail = archiveDetailResult

    CommunionHero()

    // Today's gathered passage (the anchor you respond to) + today's offering.
    TopCard {
        Eyebrow("Today's Reading · ${reading?.dateLabel ?: ""}".trimEnd(' ', '·'))
        Spacer(Modifier.height(6.dp))
        CardHeading(reading?.display ?: "Today's Reading")
        if (reading != null && reading.verses.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                reading.verses.joinToString(" ") { it.text },
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Serif,
                fontSize = 16.sp,
                lineHeight = 28.sp
            )
            Spacer(Modifier.height(16.dp))
            SecondaryAction("Read the chapter") {
                onReadChapter(reading.reference.book, reading.reference.chapter)
            }
        }

        Spacer(Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline))
        Spacer(Modifier.height(24.dp))

        Eyebrow("Today's Offering")
        Spacer(Modifier.height(6.dp))
        CardHeading("Bring one verse")
        Spacer(Modifier.height(10.dp))
        Text(
            "Bring one Scripture reference that connects with today's reading. One offering per day. 1–3 consecutive verses.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            lineHeight = 24.sp
        )
        Spacer(Modifier.height(16.dp))
        if (hasOffered) {
            Text(
                "You have brought your offering today. Return tomorrow for a new reading.",
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 22.sp
            )
        } else if (bibleData != null) {
            PrimaryAction(if (showOfferForm) "Close" else "Bring one verse") {
                showOfferForm = !showOfferForm
            }
            if (showOfferForm) {
                Spacer(Modifier.height(20.dp))
                OfferingForm(bibleData = bibleData, hasOffered = hasOffered, onSubmitted = { slug, ch, start, end ->
                    hasOffered = true
                    showOfferToast = true
                    appSettings?.update { copy(offeredTodayIso = todayIso) }
                    onOffer(slug, ch, start, end)
                })
            }
        }
    }

    TabCard {
        Eyebrow("Today's Communion · ${kept?.date ?: ""}".trimEnd(' ', '·'))
        Spacer(Modifier.height(6.dp))
        CardHeading("The witness gathered")
        Spacer(Modifier.height(4.dp))
        Text(
            "Gathered from Scripture offered by readers. The common witness rises clearly; the hidden witness is not forgotten.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            lineHeight = 21.sp
        )
        Spacer(Modifier.height(18.dp))
        if (kept == null) {
            LoadingText("Gathering today's Communion...")
        } else {
            WitnessSections(kept, onReadChapter)
        }
    }

    // Archive card with responsive grid layout
    SectionCard {
        CardHeading("Archive")
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

    // Offering submission toast
    if (showOfferToast) {
        LaunchedEffect(Unit) {
            delay(3000)
            showOfferToast = false
        }
        ToastBar("Your witness is kept. The Word answers the Word.", visible = showOfferToast)
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
        eyebrow = "Communion",
        title = detail.dateLabel
    ) {
        LazyColumn(modifier = Modifier.padding(25.dp)) {
            // Gathered passage reference
            item {
                CardHeading(detail.gatheredRef)
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
                    "HIDDEN WITNESS",
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

// --- Cascading Offering Form ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferingForm(bibleData: BibleData, hasOffered: Boolean, onSubmitted: (bookSlug: String, chapter: Int, start: Int, end: Int) -> Unit) {
    val books = bibleData.manifest.books
    var selectedBookIndex by remember { mutableIntStateOf(0) }
    var selectedChapter by remember { mutableIntStateOf(1) }
    var selectedStartVerse by remember { mutableIntStateOf(1) }
    var selectedEndVerse by remember { mutableIntStateOf(1) }
    var versePreview by remember { mutableStateOf("") }
    var maxVerse by remember { mutableIntStateOf(50) }

    val selectedBook = books[selectedBookIndex]
    val maxChapters = selectedBook.chapters

    // Load actual verse count when book/chapter changes
    LaunchedEffect(selectedBook.slug, selectedChapter) {
        try {
            val book = bibleData.book(selectedBook.slug)
            val chapterData = book.chapters.getOrNull(selectedChapter - 1)
            if (chapterData != null) {
                maxVerse = chapterData.verses.size
            }
        } catch (_: Exception) { }
    }

    // Load verse preview when selections change
    LaunchedEffect(selectedBook.slug, selectedChapter, selectedStartVerse, selectedEndVerse) {
        try {
            val book = bibleData.book(selectedBook.slug)
            val chapterData = book.chapters.getOrNull(selectedChapter - 1)
            if (chapterData != null) {
                val end = selectedEndVerse.coerceAtMost(chapterData.verses.size)
                val start = selectedStartVerse.coerceAtMost(end)
                val verses = chapterData.verses.filter { it.number in start..end }
                versePreview = if (verses.isNotEmpty()) {
                    "${selectedBook.name} ${selectedChapter}:${start}-${end}\n" +
                        verses.joinToString(" ") { it.text }
                } else ""
            }
        } catch (_: Exception) { versePreview = "" }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CascadingDropdown(
            label = "Book",
            options = books.map { it.name },
            selectedIndex = selectedBookIndex,
            onSelect = { idx ->
                selectedBookIndex = idx
                selectedChapter = 1
                selectedStartVerse = 1
                selectedEndVerse = 1
            }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CascadingDropdown(
                label = "Chapter",
                options = (1..maxChapters).map { it.toString() },
                selectedIndex = selectedChapter - 1,
                onSelect = { idx ->
                    selectedChapter = idx + 1
                    selectedStartVerse = 1
                    selectedEndVerse = 1
                },
                modifier = Modifier.weight(1f)
            )
            CascadingDropdown(
                label = "Start",
                options = (1..maxVerse).map { it.toString() },
                selectedIndex = (selectedStartVerse - 1).coerceIn(0, maxVerse - 1),
                onSelect = { idx ->
                    selectedStartVerse = idx + 1
                    if (selectedEndVerse < selectedStartVerse) selectedEndVerse = selectedStartVerse
                },
                modifier = Modifier.weight(1f)
            )
            CascadingDropdown(
                label = "End",
                options = (selectedStartVerse..maxVerse.coerceAtMost(selectedStartVerse + 2)).map { it.toString() },
                selectedIndex = (selectedEndVerse - selectedStartVerse).coerceAtLeast(0),
                onSelect = { idx ->
                    selectedEndVerse = selectedStartVerse + idx
                },
                modifier = Modifier.weight(1f)
            )
        }
    }

    // Verse preview
    if (versePreview.isNotBlank()) {
        Spacer(Modifier.height(12.dp))
        Text(
            versePreview,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Serif,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                .padding(12.dp)
        )
    }

    Spacer(Modifier.height(14.dp))
    Text(
        "After you offer, this form closes for today. The witness is gathered quietly from all offerings.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 13.sp,
        lineHeight = 21.sp
    )
    Spacer(Modifier.height(14.dp))
    if (hasOffered) {
        Text(
            "Your witness is kept.",
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 22.sp
        )
    } else {
        PrimaryAction("Bring one verse") {
            onSubmitted(selectedBook.slug, selectedChapter, selectedStartVerse, selectedEndVerse.coerceAtLeast(selectedStartVerse))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CascadingDropdown(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = options.getOrElse(selectedIndex) { "Select" }

    Column(modifier = modifier) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelect(index)
                            expanded = false
                        }
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
            "Read today's word, bring one verse, and see the witness the Word gathers.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 15.sp,
            lineHeight = 25.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 560.dp)
        )
    }
}

// --- Witness sections: Common Witness + Hidden Witness (references only) ---

@Composable
fun WitnessSections(communion: KeptCommunion, onReadChapter: (String, Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (communion.common.isEmpty() && communion.hidden.isEmpty()) {
            Text(
                "No verses have been brought yet today. You may be the first to bring a witness.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
            return@Column
        }
        if (communion.common.isNotEmpty()) {
            WitnessLabel("COMMON WITNESS")
            communion.common.forEach { WitnessItem(it, onReadChapter) }
        }
        if (communion.hidden.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            WitnessLabel("HIDDEN WITNESS")
            communion.hidden.forEach { WitnessItem(it, onReadChapter) }
        }
    }
}

@Composable
private fun WitnessLabel(text: String) {
    Text(
        text,
        color = MaterialTheme.colorScheme.secondary,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun WitnessItem(entry: CommunionEntry, onReadChapter: (String, Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                entry.display,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Serif,
                fontSize = 15.sp,
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
                fontFamily = FontFamily.Serif,
                fontSize = 14.sp,
                lineHeight = 24.sp
            )
        }
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
        )
    }
}

/** Tappable "Read the chapter" link used across the witness sections. */
@Composable
private fun ReadChapterLink(bookSlug: String, chapter: Int, onReadChapter: (String, Int) -> Unit) {
    Text(
        "Read the chapter",
        color = MaterialTheme.colorScheme.secondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable { onReadChapter(bookSlug, chapter) }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

// --- Top card with accent top border (web .communion-top-card) ---

@Composable
fun TopCard(content: @Composable ColumnScope.() -> Unit) {
    StaggeredEntrance(delayMillis = 80) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .border(2.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .padding(horizontal = 24.dp, vertical = 26.dp),
                content = content
            )
        }
    }
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
