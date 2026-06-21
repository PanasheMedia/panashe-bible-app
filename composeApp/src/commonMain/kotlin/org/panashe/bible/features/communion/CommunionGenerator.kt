package org.panashe.bible.features.communion

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.panashe.bible.BibleManifest
import org.panashe.bible.CommunionSeed
import org.panashe.bible.shared.ScriptureReference
import org.panashe.bible.shared.SharedRules

/**
 * Derives the Communion for a given date, mirroring panashe-bible-web's
 * communion-seed.js (buildSeededCommunions / referenceFor / generatedReferenceFor)
 * so the app and web render identical days. Each day consumes seven slots
 * (1 gathered + 6 offerings) from the flattened seed reference pool; once the
 * pool is exhausted, references are generated deterministically by walking the
 * flat chapter list at (index * 7 + offset) mod totalChapters.
 */
class CommunionGenerator(
    manifest: BibleManifest,
    private val seed: CommunionSeed,
) {
    /** Flattened seed pool: each theme contributes its gathered ref then its offerings. */
    private val pool: List<ScriptureReference> =
        seed.themes.flatMap { listOf(it.gathered) + it.offerings }

    /** Every [bookSlug, chapter] pair across the manifest, in canonical order. */
    private val chapters: List<Pair<String, Int>> =
        manifest.books.flatMap { book -> (1..book.chapters).map { book.slug to it } }

    private val startEpochDay = LocalDate.parse(seed.startIso).toEpochDays()

    /** Days from the seed start to [iso] (0 == start date). */
    fun dayIndexForIso(iso: String): Int =
        LocalDate.parse(iso).toEpochDays() - startEpochDay

    /** ISO date (yyyy-MM-dd) for a day [index] offset from the seed start. */
    fun isoForIndex(index: Int): String =
        LocalDate.fromEpochDays(startEpochDay + index).toString()

    /** Total number of seeded days, inclusive of start and end. */
    val totalDays: Int get() = dayIndexForIso(seed.endIso) + 1

    private fun generatedReference(index: Int, offset: Int): ScriptureReference {
        val slot = ((index * 7 + offset) % chapters.size + chapters.size) % chapters.size
        val (slug, chapter) = chapters[slot]
        return SharedRules.canonicalReference(book = slug, chapter = chapter, startVerse = 1, endVerse = 1)
    }

    private fun referenceFor(index: Int, offset: Int): ScriptureReference {
        val poolIndex = index * 7 + offset
        return if (poolIndex < pool.size) pool[poolIndex] else generatedReference(index, offset)
    }

    /** The Communion references for [iso], clamped into the seeded range. */
    fun communionForDate(iso: String): CommunionDayRefs {
        val index = dayIndexForIso(iso).coerceIn(0, (totalDays - 1).coerceAtLeast(0))
        val gathered = referenceFor(index, 0)
        // Six beneath offerings, de-duplicated like web's uniqueByReference.
        val offerings = (1..6)
            .map { referenceFor(index, it) }
            .distinctBy { SharedRules.referenceKey(it) }
        return CommunionDayRefs(
            index = index,
            iso = isoForIndex(index),
            gathered = gathered,
            offerings = offerings,
        )
    }

    /** The Communion for today (UTC, matching the seed's COMMUNION_TIMEZONE). */
    fun communionForToday(): CommunionDayRefs =
        communionForDate(todayIso())

    companion object {
        /** Today's date at UTC midnight as yyyy-MM-dd, mirroring web's todayISO(). */
        fun todayIso(): String = Clock.System.todayIn(TimeZone.UTC).toString()
    }
}

/** Resolved Communion references for a single day. */
data class CommunionDayRefs(
    val index: Int,
    val iso: String,
    val gathered: ScriptureReference,
    val offerings: List<ScriptureReference>,
)
