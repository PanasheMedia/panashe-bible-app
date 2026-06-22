package org.panashe.bible

import org.panashe.bible.features.communion.CommunionEntry
import org.panashe.bible.features.communion.CommunionGenerator
import org.panashe.bible.features.communion.CommunionView
import org.panashe.bible.features.communion.KeptCommunion
import org.panashe.bible.features.reader.DailyReading
import org.panashe.bible.shared.SharedConstants

// Routes mirror panashe-bible-shared PUBLIC_ROUTES (single-sourced via SharedConstants).
enum class PanasheRoute(val path: String, val title: String) {
    Daily(SharedConstants.ROUTE_DAILY, "Daily"),
    Bible(SharedConstants.ROUTE_BIBLE, "Scripture"),
    Communion(SharedConstants.ROUTE_COMMUNION, "Communion"),
    About(SharedConstants.ROUTE_ABOUT, "About"),
    Privacy(SharedConstants.ROUTE_PRIVACY, "Privacy")
}

/**
 * Builds the daily reading and kept Communion from the bundled seed and shared
 * rules. No Scripture text or references are hardcoded here; they are resolved
 * from the canonical data exported by panashe-bible-shared.
 */
suspend fun buildCommunionView(data: BibleData): CommunionView {
    val day = CommunionGenerator(data.manifest, data.seed).communionForToday()
    val dateLabel = formatDate(day.iso)
    val gatheredRef = day.gathered
    val gatheredBook = data.book(gatheredRef.book)
    val gatheredChapter = gatheredBook.chapter(gatheredRef.chapter)

    val reading = DailyReading(
        dateLabel = dateLabel,
        reference = gatheredRef,
        display = data.displayReference(gatheredRef),
        chapterTitle = "${gatheredBook.name} ${gatheredRef.chapter}",
        chapterIntro = gatheredChapter?.introduction ?: gatheredBook.introduction,
        verses = gatheredChapter?.verseRange(gatheredRef.startVerse, gatheredRef.endVerse).orEmpty(),
        chapterVerses = gatheredChapter?.verses.orEmpty()
    )

    // Seed fallback (offline): the day's offerings form the thread, most-resonant
    // first. The live thread comes from the API.
    val thread = day.offerings.take(7).map { ref ->
        CommunionEntry(ref, data.displayReference(ref), data.passageText(ref), "")
    }
    val gatheredEntry = thread.firstOrNull() ?: CommunionEntry(
        gatheredRef, data.displayReference(gatheredRef), data.passageText(gatheredRef), ""
    )

    return CommunionView(
        reading = reading,
        kept = KeptCommunion(
            date = dateLabel,
            gathered = gatheredEntry,
            beneath = thread.drop(1)
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
    "Daily Communion has two public surfaces: Today's Reading and Today's Communion.",
    "Readers offer complete Scripture references that connect with the day's reading. Those verses gather into the day's Communion.",
    "Today's Communion shows the verses interacting today, gathered around the Word — without names, counts, comments, or rankings."
)

val privacyParagraphs = listOf(
    "Reading preferences stay on the device.",
    "Communion offerings are received without public identity. Counts and non-kept offerings are not shown publicly.",
    "Cloudflare may process basic request data to serve the site and protect it from abuse."
)
