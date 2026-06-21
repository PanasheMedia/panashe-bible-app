package org.panashe.bible

import org.panashe.bible.shared.ScriptureReference
import org.panashe.bible.shared.SharedConstants
import org.panashe.bible.shared.SharedRules

// Routes mirror panashe-bible-shared PUBLIC_ROUTES (single-sourced via SharedConstants).
enum class PanasheRoute(val path: String, val title: String) {
    Daily(SharedConstants.ROUTE_DAILY, "Daily"),
    Bible(SharedConstants.ROUTE_BIBLE, "Scripture"),
    Communion(SharedConstants.ROUTE_COMMUNION, "Communion"),
    About(SharedConstants.ROUTE_ABOUT, "About"),
    Privacy(SharedConstants.ROUTE_PRIVACY, "Privacy")
}

/** A Communion entry with its display reference and the resolved Scripture text. */
data class CommunionEntry(
    val reference: ScriptureReference,
    val display: String,
    val preview: String,
    val state: String
)

/** The kept seven: one gathered passage with [SharedConstants.KEPT_BENEATH_COUNT] beneath. */
data class KeptCommunion(
    val date: String,
    val gathered: CommunionEntry,
    val beneath: List<CommunionEntry>
)

/** Today's reading derived from the seed's gathered passage. */
data class DailyReading(
    val dateLabel: String,
    val reference: ScriptureReference,
    val display: String,
    val chapterTitle: String,
    val chapterIntro: String,
    val verses: List<BibleVerse>,
    val chapterVerses: List<BibleVerse>
)

/** Everything the screens need, derived from bundled data + shared rules. */
data class CommunionView(
    val reading: DailyReading,
    val kept: KeptCommunion
)

/**
 * Builds the daily reading and kept Communion from the bundled seed and shared
 * rules. No Scripture text or references are hardcoded here; they are resolved
 * from the canonical data exported by panashe-bible-shared.
 */
suspend fun buildCommunionView(data: BibleData): CommunionView {
    val theme = data.seed.first
    val gatheredRef = theme.gathered
    val gatheredBook = data.book(gatheredRef.book)
    val gatheredChapter = gatheredBook.chapter(gatheredRef.chapter)

    val reading = DailyReading(
        dateLabel = formatDate(data.seed.startIso),
        reference = gatheredRef,
        display = data.displayReference(gatheredRef),
        chapterTitle = "${gatheredBook.name} ${gatheredRef.chapter}",
        chapterIntro = gatheredChapter?.introduction ?: gatheredBook.introduction,
        verses = gatheredChapter?.verseRange(gatheredRef.startVerse, gatheredRef.endVerse).orEmpty(),
        chapterVerses = gatheredChapter?.verses.orEmpty()
    )

    val gatheredEntry = CommunionEntry(
        reference = gatheredRef,
        display = data.displayReference(gatheredRef),
        preview = data.passageText(gatheredRef),
        state = "Gathered passage"
    )

    val beneath = theme.offerings.map { ref ->
        CommunionEntry(
            reference = ref,
            display = data.displayReference(ref),
            preview = data.passageText(ref),
            state = "Kept beneath"
        )
    }

    return CommunionView(
        reading = reading,
        kept = KeptCommunion(
            date = formatDate(data.seed.startIso),
            gathered = gatheredEntry,
            beneath = beneath
        )
    )
}

/** Formats an ISO date (yyyy-MM-dd) as "Month D, YYYY" without platform date APIs. */
fun formatDate(iso: String): String {
    val parts = iso.split("-")
    if (parts.size != 3) return iso
    val year = parts[0]
    val month = parts[1].toIntOrNull() ?: return iso
    val day = parts[2].toIntOrNull()?.toString() ?: parts[2]
    val monthName = MONTHS.getOrNull(month - 1) ?: return iso
    return "$monthName $day, $year"
}

private val MONTHS = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

val aboutParagraphs = listOf(
    "Panashe speaks of the presence of God. The project is shaped to help readers dwell with Scripture, not rush past it.",
    "Daily Communion has three surfaces: Today's Reading, Today's Offering, and Today's Communion.",
    "Readers offer complete Scripture references. Matching references form a Common Witness; quieter offerings remain part of the Hidden Witness.",
    "The kept Communion is the final seven: one gathered passage with six beneath it, shown without names, counts, comments, or rankings."
)

val privacyParagraphs = listOf(
    "Reading preferences stay on the device.",
    "Communion offerings are received without public identity. Counts and non-kept offerings are not shown publicly.",
    "Cloudflare may process basic request data to serve the site and protect it from abuse."
)
