package org.panashe.bible

import org.panashe.bible.features.communion.CommunionGenerator
import org.panashe.bible.shared.ScriptureReference
import org.panashe.bible.shared.SharedRules
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Locks CommunionGenerator to the app's pre-DB placeholder behavior.
 *
 * The bundled seed is proof-of-concept data. It may cover a full date range
 * before all 365 entries are hand-planned, so the app cycles explicit seed
 * themes instead of generating arbitrary chapter-walk references.
 */
class CommunionGeneratorTest {
    private val manifest = BibleManifest(
        translation = TranslationInfo("KJVA", "kjva", "King James Version with Apocrypha"),
        books = listOf(
            BookSummary(index = 0, name = "Alpha", slug = "a", chapters = 3, section = "Old Testament"),
            BookSummary(index = 1, name = "Beta", slug = "b", chapters = 2, section = "Old Testament"),
        ),
    )

    private fun ref(book: String, chapter: Int) =
        SharedRules.canonicalReference(book, chapter, 1, 1)

    private val seed = CommunionSeed(
        startIso = "2026-06-01",
        endIso = "2026-06-04",
        themes = listOf(
            CommunionTheme(
                gathered = ref("a", 1),
                offerings = listOf(ref("a", 2), ref("b", 1), ref("b", 1)),
            ),
            CommunionTheme(
                gathered = ref("b", 2),
                offerings = listOf(ref("a", 3), ref("b", 1)),
            ),
        ),
    )

    private val generator = CommunionGenerator(manifest, seed)

    private fun key(reference: ScriptureReference) = SharedRules.referenceKey(reference)

    @Test
    fun dateMath() {
        assertEquals(0, generator.dayIndexForIso("2026-06-01"))
        assertEquals(20, generator.dayIndexForIso("2026-06-21"))
        assertEquals("2026-06-01", generator.isoForIndex(0))
        assertEquals("2026-06-03", generator.isoForIndex(2))
        assertEquals(4, generator.totalDays)
    }

    @Test
    fun day0DrawsFromFirstSeedTheme() {
        val day = generator.communionForDate("2026-06-01")
        assertEquals(0, day.index)
        assertEquals(key(ref("a", 1)), key(day.gathered))
        assertEquals(
            listOf(key(ref("a", 2)), key(ref("b", 1))),
            day.offerings.map { key(it) },
        )
    }

    @Test
    fun day1DrawsFromSecondSeedTheme() {
        val day = generator.communionForDate("2026-06-02")
        assertEquals(1, day.index)
        assertEquals(key(ref("b", 2)), key(day.gathered))
        assertEquals(
            listOf(key(ref("a", 3)), key(ref("b", 1))),
            day.offerings.map { key(it) },
        )
    }

    @Test
    fun placeholderDaysCycleExplicitThemes() {
        val day = generator.communionForDate("2026-06-03")
        assertEquals(2, day.index)
        assertEquals(key(ref("a", 1)), key(day.gathered))
        assertEquals(
            listOf(key(ref("a", 2)), key(ref("b", 1))),
            day.offerings.map { key(it) },
        )
    }

    @Test
    fun datesPastSeedRangeClampToLastDay() {
        val day = generator.communionForDate("2030-01-01")
        assertEquals(generator.totalDays - 1, day.index)
        assertEquals(key(ref("b", 2)), key(day.gathered))
    }
}
