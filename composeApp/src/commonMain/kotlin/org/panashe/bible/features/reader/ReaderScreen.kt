package org.panashe.bible.features.reader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.panashe.bible.BibleData
import org.panashe.bible.BibleVerse
import org.panashe.bible.BookSummary
import org.panashe.bible.features.audio.TtsEngine
import org.panashe.bible.features.audio.createTtsEngine
import org.panashe.bible.features.communion.CommunionView
import org.panashe.bible.ui.Accent
import org.panashe.bible.ui.Ink
import org.panashe.bible.ui.Line
import org.panashe.bible.ui.Muted
import org.panashe.bible.ui.Soft
import org.panashe.bible.ui.SurfaceColor
import org.panashe.bible.ui.components.CloseIcon
import org.panashe.bible.ui.components.Eyebrow
import org.panashe.bible.ui.components.Hero
import org.panashe.bible.ui.components.LoadingText
import org.panashe.bible.ui.components.PanasheDialog
import org.panashe.bible.ui.components.PlayIcon
import org.panashe.bible.ui.components.PrimaryAction
import org.panashe.bible.ui.components.SecondaryAction
import org.panashe.bible.ui.components.SectionCard
import androidx.compose.ui.platform.LocalClipboardManager

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyReadingScreen(view: CommunionView?, loadError: String?, onBible: () -> Unit) {
    val reading = view?.reading
    val clipboardManager = LocalClipboardManager.current

    // Daily hero — mirrors web .daily-hero
    Hero(
        eyebrow = reading?.dateLabel,
        title = "Daily Reading",
        intro = "A daily portion of Holy Scripture for reading, remembrance, and obedience before God."
    )

    // Single passage card — mirrors web .daily-card
    SectionCard {
        Eyebrow(reading?.dateLabel ?: "Today's Reading")
        Spacer(Modifier.height(10.dp))
        Text(
            reading?.display ?: "Loading...",
            color = Ink,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 28.sp
        )
        Spacer(Modifier.height(14.dp))
        Text(
            reading?.chapterIntro ?: "Loading the chapter context from bundled Scripture.",
            color = Muted,
            fontSize = 14.sp,
            lineHeight = 24.sp
        )
        Spacer(Modifier.height(16.dp))
        when {
            loadError != null -> LoadingText(loadError)
            reading == null -> LoadingText("Loading Scripture...")
            else -> reading.verses.forEach { verse ->
                Text(
                    text = verse.text,
                    color = Ink,
                    fontFamily = FontFamily.Serif,
                    fontSize = 18.sp,
                    lineHeight = 34.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }

        // Button divider — mirrors web .daily-actions top border
        Spacer(Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Line))
        Spacer(Modifier.height(18.dp))

        // Action buttons
        FlowRow(horizontalArrangement = Arrangement.Center, verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            PrimaryAction("Read full chapter") { onBible() }
            SecondaryAction("Copy passage") {
                if (reading != null) {
                    val passageText = reading.verses.joinToString(" ") { it.text }
                    val fullText = "${reading.display}\n$passageText"
                    clipboardManager.setText(AnnotatedString(fullText))
                }
            }
            SecondaryAction("Browse the Bible", onBible)
        }
    }
}

@Composable
fun BibleScreen(
    view: CommunionView?,
    loadError: String?,
    bibleData: BibleData?,
    bookSlug: String,
    chapter: Int,
    onBookChange: (String) -> Unit,
    onChapterChange: (Int) -> Unit,
    prefs: MutableReaderPreferences = MutableReaderPreferences()
) {
    val books = bibleData?.manifest?.books ?: emptyList()
    val bookSummary = books.firstOrNull { it.slug == bookSlug } ?: books.firstOrNull()

    // Load verses from BibleData
    var verses by remember { mutableStateOf<List<BibleVerse>>(emptyList()) }
    var loadingError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(bookSlug, chapter) {
        if (bibleData != null && bookSummary != null) {
            try {
                val book = bibleData.book(bookSummary.slug)
                val chapterData = book.chapters.getOrNull(chapter - 1)
                if (chapterData != null) {
                    verses = chapterData.verses
                    loadingError = null
                } else {
                    verses = emptyList()
                    loadingError = "Chapter $chapter not available for ${bookSummary.name}"
                }
            } catch (e: Exception) {
                verses = emptyList()
                loadingError = "Failed to load ${bookSummary.name} chapter $chapter"
            }
        }
    }

    // Navigation state
    var showBookPicker by remember { mutableStateOf(false) }
    var showChapterPicker by remember { mutableStateOf(false) }
    var showVersePicker by remember { mutableStateOf(false) }
    var showTranslationInfo by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // TTS state
    val ttsEngine = remember { createTtsEngine() }
    var isAudioPlaying by remember { mutableStateOf(false) }

    val previousChapter = if (chapter > 1) chapter - 1 else null
    val nextChapter = if (bookSummary != null && chapter < bookSummary.chapters) chapter + 1 else null
    // If at end of book, next goes to first chapter of next book
    val nextBookSlug = if (nextChapter == null && bookSummary != null) {
        books.getOrNull(books.indexOf(bookSummary) + 1)?.slug
    } else null
    val nextChapterForNav = if (nextChapter == null && nextBookSlug != null) 1 else null

    // Previous book (if at chapter 1)
    val previousBookSlug = if (chapter == 1 && bookSummary != null) {
        books.getOrNull(books.indexOf(bookSummary) - 1)?.slug
    } else null
    val previousChapterForNav = if (previousChapter == null && previousBookSlug != null) {
        books.firstOrNull { it.slug == previousBookSlug }?.chapters ?: 1
    } else null

    ReaderToolbar(
        bookName = bookSummary?.name ?: "Bible",
        chapterNumber = chapter.toString(),
        onBookClick = { showBookPicker = true },
        onChapterClick = { showChapterPicker = true },
        onTranslationClick = { showTranslationInfo = true }
    )

    SectionCard {
        // Section eyebrow (Old/New Testament)
        val section = bookSummary?.section
        if (section != null) {
            Eyebrow(section)
            Spacer(Modifier.height(6.dp))
        }

        // Chapter title
        Text(
            "${bookSummary?.name ?: ""} $chapter",
            color = Ink,
            fontFamily = FontFamily.Serif,
            fontSize = 52.sp,
            lineHeight = 56.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Chapter $chapter of ${bookSummary?.name ?: "Scripture"}",
            color = Muted,
            fontSize = 13.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(14.dp))

        // Audio play button + verse count — mirrors web .chapter-actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${verses.size} verses",
                color = Muted,
                fontSize = 11.sp,
                letterSpacing = 1.1.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(12.dp))
            IconButton(
                onClick = {
                    if (isAudioPlaying) {
                        ttsEngine.stop()
                        isAudioPlaying = false
                    } else {
                        val text = verses.joinToString(" ") { it.text }
                        ttsEngine.speak(text)
                        isAudioPlaying = true
                    }
                }
            ) {
                Icon(
                    imageVector = PlayIcon,
                    contentDescription = if (isAudioPlaying) "Stop audio" else "Listen to chapter",
                    tint = Ink,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(Modifier.height(26.dp))

        // Verse picker toggle
        if (verses.isNotEmpty()) {
            Text(
                if (showVersePicker) "Hide verse picker" else "Jump to verse",
                color = Accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
                    .clickable { showVersePicker = !showVersePicker }
                    .padding(bottom = 12.dp)
            )
            if (showVersePicker) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    verses.forEach { verse ->
                        Surface(
                            color = Soft,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.clickable {
                                showVersePicker = false
                                coroutineScope.launch {
                                    val index = verses.indexOf(verse)
                                    if (index >= 0) listState.animateScrollToItem(index)
                                }
                            }
                        ) {
                            Text(
                                "${verse.number}",
                                color = Ink,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        when {
            loadError != null -> LoadingText(loadError)
            loadingError != null -> LoadingText(loadingError!!)
            verses.isEmpty() -> LoadingText("Loading Scripture...")
            else -> {
                // Render verses with prefs applied
                val snapshot = prefs.snapshot()
                verses.forEach { verse ->
                    if (snapshot.showVerseNumbers) {
                        val verseAnnotated = buildAnnotatedString {
                            withStyle(SpanStyle(
                                fontSize = (11f * snapshot.textSizeMultiplier).sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Accent
                            )) {
                                append(" ${verse.number} ")
                            }
                            withStyle(SpanStyle(
                                fontFamily = snapshot.fontFamily,
                                fontSize = snapshot.baseFontSizeSp.sp,
                                color = Ink
                            )) {
                                append(verse.text)
                            }
                        }
                        Text(
                            text = verseAnnotated,
                            color = Ink,
                            lineHeight = snapshot.lineHeightSp.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    } else {
                        Text(
                            text = verse.text,
                            color = Ink,
                            fontFamily = snapshot.fontFamily,
                            fontSize = snapshot.baseFontSizeSp.sp,
                            lineHeight = snapshot.lineHeightSp.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                // Chapter navigation (Previous/Next)
                ChapterNav(
                    previousLabel = if (previousChapter != null) {
                        "${bookSummary?.name ?: "Book"} $previousChapter"
                    } else if (previousBookSlug != null) {
                        "${books.firstOrNull { it.slug == previousBookSlug }?.name ?: "Book"} $previousChapterForNav"
                    } else null,
                    nextLabel = if (nextChapter != null) {
                        "${bookSummary?.name ?: "Book"} $nextChapter"
                    } else if (nextBookSlug != null) {
                        "${books.firstOrNull { it.slug == nextBookSlug }?.name ?: "Book"} 1"
                    } else null,
                    onPrevious = {
                        if (previousChapter != null) {
                            onChapterChange(previousChapter)
                        } else if (previousBookSlug != null) {
                            onBookChange(previousBookSlug)
                            onChapterChange(previousChapterForNav ?: 1)
                        }
                    },
                    onNext = {
                        if (nextChapter != null) {
                            onChapterChange(nextChapter)
                        } else if (nextBookSlug != null) {
                            onBookChange(nextBookSlug)
                            onChapterChange(1)
                        }
                    }
                )
            }
        }
    }

    // Book picker dialog
    if (showBookPicker) {
        BookPickerDialog(
            books = books,
            selectedSlug = bookSlug,
            onSelect = { slug ->
                onBookChange(slug)
                onChapterChange(1)
                showBookPicker = false
            },
            onDismiss = { showBookPicker = false }
        )
    }

    // Chapter picker dialog
    if (showChapterPicker && bookSummary != null) {
        ChapterPickerDialog(
            bookName = bookSummary.name,
            chapterCount = bookSummary.chapters,
            selectedChapter = chapter,
            onSelect = { ch ->
                onChapterChange(ch)
                showChapterPicker = false
            },
            onDismiss = { showChapterPicker = false }
        )
    }

    // Translation info dialog
    if (showTranslationInfo) {
        TranslationInfoDialog(
            onDismiss = { showTranslationInfo = false }
        )
    }

    // Stop audio on chapter change
    LaunchedEffect(bookSlug, chapter) {
        ttsEngine.stop()
        isAudioPlaying = false
    }
}

@Composable
fun ReaderToolbar(
    bookName: String,
    chapterNumber: String,
    translationName: String = "KJVA",
    onBookClick: () -> Unit,
    onChapterClick: () -> Unit,
    onTranslationClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(Soft)
            .border(BorderStroke(1.dp, Line))
            .padding(horizontal = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToolbarSelector("Book", bookName, Modifier.weight(1f).clickable { onBookClick() })
        Box(modifier = Modifier.width(1.dp).height(44.dp).background(Line))
        ToolbarSelector("Chapter", chapterNumber, Modifier.weight(1f).clickable { onChapterClick() })
        Box(modifier = Modifier.width(1.dp).height(44.dp).background(Line))
        ToolbarSelector("Translation", translationName, Modifier.weight(0.8f).clickable { onTranslationClick() })
    }
}

@Composable
fun ToolbarSelector(label: String, value: String, modifier: Modifier) {
    Column(modifier = modifier.padding(14.dp)) {
        Eyebrow(label)
        Text(value, color = Ink, fontFamily = FontFamily.Serif, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun TranslationInfoCard(translationName: String = "KJVA", attribution: String = "") {
    Column(modifier = Modifier.padding(25.dp)) {
        Text(
            translationName,
            color = Ink,
            fontFamily = FontFamily.Serif,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "King James Version with Apocrypha (KJVA) — the Authorized Version of 1611, including the Apocryphal books between the Old and New Testaments.",
            color = Muted,
            fontSize = 13.sp,
            lineHeight = 22.sp
        )
        if (attribution.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(attribution, color = Muted, fontSize = 11.sp, lineHeight = 16.sp)
        }
    }
}

@Composable
fun ChapterNav(previousLabel: String?, nextLabel: String?, onPrevious: () -> Unit, onNext: () -> Unit) {
    Spacer(Modifier.height(40.dp))
    Row(
        modifier = Modifier.fillMaxWidth()
            .border(BorderStroke(1.dp, Line), RoundedCornerShape(4.dp))
            .padding(18.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (previousLabel != null) {
            Column(modifier = Modifier.weight(1f).clickable { onPrevious() }) {
                Eyebrow("Previous")
                Spacer(Modifier.height(4.dp))
                Text(previousLabel, fontFamily = FontFamily.Serif, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        } else {
            Spacer(Modifier.weight(1f))
        }
        if (nextLabel != null) {
            Column(modifier = Modifier.weight(1f).clickable { onNext() }, horizontalAlignment = Alignment.End) {
                Eyebrow("Next")
                Spacer(Modifier.height(4.dp))
                Text(nextLabel, fontFamily = FontFamily.Serif, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// --- Book Picker Dialog ---

@Composable
fun BookPickerDialog(
    books: List<BookSummary>,
    selectedSlug: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sections = books.groupBy { it.section }

    PanasheDialog(
        onDismissRequest = onDismiss,
        eyebrow = "Navigate",
        title = "Books"
    ) {
        LazyColumn(modifier = Modifier.heightIn(max = 600.dp)) {
            sections.forEach { (section, sectionBooks) ->
                item {
                    Text(
                        section.uppercase(),
                        color = Accent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(start = 25.dp, top = if (section == "Old Testament") 18.dp else 16.dp, bottom = 8.dp, end = 25.dp)
                    )
                }
                items(sectionBooks) { book ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable { onSelect(book.slug) }
                            .background(
                                if (book.slug == selectedSlug) Accent.copy(alpha = 0.08f) else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 25.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                book.name,
                                color = Ink,
                                fontFamily = FontFamily.Serif,
                                fontSize = 16.sp,
                                fontWeight = if (book.slug == selectedSlug) FontWeight.SemiBold else FontWeight.Normal
                            )
                            if (book.description.isNotBlank()) {
                                Text(
                                    "${book.description} \u00B7 ${book.chapters} chapters",
                                    color = Muted,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                        if (book.slug == selectedSlug) {
                            Text("\u2713", color = Accent, fontSize = 16.sp)
                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).padding(start = 25.dp).background(Line.copy(alpha = 0.5f)))
                }
            }
        }
    }
}

// --- Translation Info Dialog ---

@Composable
fun TranslationInfoDialog(onDismiss: () -> Unit) {
    PanasheDialog(
        onDismissRequest = onDismiss,
        eyebrow = "Translation",
        title = "KJVA"
    ) {
        Column(modifier = Modifier.padding(25.dp)) {
            Text(
                "King James Version with Apocrypha",
                color = Ink,
                fontFamily = FontFamily.Serif,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "The Authorized Version of 1611 (KJV), including the Apocryphal books between the Old and New Testaments. The text has been updated for modern readability while preserving the dignity of the original translation.",
                color = Muted,
                fontSize = 13.sp,
                lineHeight = 22.sp
            )
        }
    }
}

// --- Chapter Picker Dialog ---

@Composable
fun ChapterPickerDialog(
    bookName: String,
    chapterCount: Int,
    selectedChapter: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    PanasheDialog(
        onDismissRequest = onDismiss,
        eyebrow = bookName,
        title = "Select chapter"
    ) {
        Column(modifier = Modifier.padding(25.dp)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 500.dp)
            ) {
                for (ch in 1..chapterCount) {
                    Surface(
                        color = if (ch == selectedChapter) Accent else Soft,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable { onSelect(ch) }
                    ) {
                        Text(
                            ch.toString(),
                            color = if (ch == selectedChapter) SurfaceColor else Ink,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}
