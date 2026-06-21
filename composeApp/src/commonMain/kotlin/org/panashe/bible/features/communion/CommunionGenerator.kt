package org.panashe.bible.features.communion

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.panashe.bible.BibleManifest
import org.panashe.bible.CommunionSeed
import org.panashe.bible.CommunionTheme
import org.panashe.bible.shared.ScriptureReference
import org.panashe.bible.shared.SharedConstants
import org.panashe.bible.shared.SharedRules

/**
 * Derives the Communion for a given date from the bundled pre-seed plan.
 *
 * The bundled seed is a proof-of-concept placeholder until submitted Communion
 * data can be read from a database. Because placeholder data should not invent
 * "inspired" daily pairings, the generator only uses explicit seed themes. When
 * the date range is longer than the available themes, it cycles those themes
 * rather than walking arbitrary Bible chapters.
 */
class CommunionGenerator(
    @Suppress("UNUSED_PARAMETER") manifest: BibleManifest,
    private val seed: CommunionSeed,
) {
    private val startEpochDay = LocalDate.parse(seed.startIso).toEpochDays()

    /** Days from the seed start to [iso] (0 == start date). */
    fun dayIndexForIso(iso: String): Int =
        LocalDate.parse(iso).toEpochDays() - startEpochDay

    /** ISO date (yyyy-MM-dd) for a day [index] offset from the seed start. */
    fun isoForIndex(index: Int): String =
        LocalDate.fromEpochDays(startEpochDay + index).toString()

    /** Total number of seeded days, inclusive of start and end. */
    val totalDays: Int get() = dayIndexForIso(seed.endIso) + 1

    /** ISO date for [baseIso] + [offsetDays], or null if out of range. */
    fun isoForDate(baseIso: String, offsetDays: Int): String? {
        val baseIndex = dayIndexForIso(baseIso)
        val targetIndex = baseIndex + offsetDays
        if (targetIndex < 0 || targetIndex >= totalDays) return null
        return isoForIndex(targetIndex)
    }

    private fun themeFor(index: Int): CommunionTheme {
        require(seed.themes.isNotEmpty()) { "Communion seed must include at least one theme." }
        val themeIndex = ((index % seed.themes.size) + seed.themes.size) % seed.themes.size
        return seed.themes[themeIndex]
    }

    /** The Communion references for [iso], clamped into the seeded range. */
    fun communionForDate(iso: String): CommunionDayRefs {
        val index = dayIndexForIso(iso).coerceIn(0, (totalDays - 1).coerceAtLeast(0))
        val theme = themeFor(index)
        val gathered = theme.gathered
        val offerings = theme.offerings
            .distinctBy { SharedRules.referenceKey(it) }
            .take(SharedConstants.KEPT_BENEATH_COUNT)
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
