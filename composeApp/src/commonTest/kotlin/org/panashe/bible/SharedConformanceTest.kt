package org.panashe.bible

import org.panashe.bible.features.communion.CommunionGenerator
import org.panashe.bible.shared.ScriptureReference
import org.panashe.bible.shared.SharedRules
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Mirrors panashe-bible-shared/fixtures/conformance.json. The web platform
 * asserts the same cases in scripts/validate-fixtures.mjs. Keep these in sync;
 * if the shared rules change, update fixtures/conformance.json and re-export.
 */
class SharedConformanceTest {
    private fun ref(book: String, chapter: Int, start: Int, end: Int = start) =
        SharedRules.canonicalReference(book, chapter, start, end)

    @Test
    fun referenceVerseCount() {
        assertEquals(3, SharedRules.referenceVerseCount(ref("john", 1, 1, 3)))
        assertEquals(1, SharedRules.referenceVerseCount(ref("psalms", 133, 1, 1)))
        assertEquals(2, SharedRules.referenceVerseCount(ref("i-john", 4, 7, 8)))
    }

    @Test
    fun formatReference() {
        assertEquals("John 1:1-3", SharedRules.formatReference(ref("john", 1, 1, 3), "John"))
        assertEquals("Psalms 133:1", SharedRules.formatReference(ref("psalms", 133, 1, 1), "Psalms"))
        assertEquals("I Peter 1:22", SharedRules.formatReference(ref("i-peter", 1, 22, 22), "I Peter"))
    }

    @Test
    fun referenceKey() {
        assertEquals("kjva:john:1:1:3", SharedRules.referenceKey(ref("john", 1, 1, 3)))
        assertEquals("kjva:romans:12:10:10", SharedRules.referenceKey(ref("romans", 12, 10, 10)))
    }

    @Test
    fun chapterAndVersePaths() {
        assertEquals("/bible/kjva/john/1", SharedRules.chapterPath(ref("john", 1, 1, 3)))
        assertEquals("/bible/kjva/psalms/133", SharedRules.chapterPath(ref("psalms", 133, 1, 1)))
        assertEquals("/bible/kjva/john/1/verse-1", SharedRules.versePath(ref("john", 1, 1, 3)))
    }

    @Test
    fun communionArchivePath() {
        assertEquals("/archive/2026-06-01", SharedRules.communionArchivePath("2026-06-01"))
    }

    @Test
    fun validOffering() {
        assertTrue(SharedRules.isValidOffering(ref("john", 1, 1, 3)))
        assertTrue(SharedRules.isValidOffering(ref("psalms", 133, 1, 1)))
        // 4 verses exceeds the offering window.
        assertFalse(SharedRules.isValidOffering(ScriptureReference("kjva", "john", 1, 1, 4)))
    }

    // ── Communion seed conformance ──────────────────────────────────────
    // Mirrors the communionSeedDate section of conformance.json.
    // Uses the first 3 themes from the canonical seed to verify the
    // Kotlin CommunionGenerator matches the JS buildSeededCommunions.

    private fun bookSummary(index: Int, name: String, slug: String, chapters: Int) =
        BookSummary(index = index, name = name, slug = slug, chapters = chapters, section = "Canon")

    private val seedManifest = BibleManifest(
        translation = TranslationInfo("KJVA", "kjva", "King James Version with Apocrypha"),
        books = listOf(
            bookSummary(0, "John", "john", 21),
            bookSummary(1, "Hebrews", "hebrews", 13),
            bookSummary(2, "Proverbs", "proverbs", 31),
        ),
    )

    private val seed = CommunionSeed(
        startIso = "2026-06-01",
        endIso = "2026-06-28",
        themes = listOf(
            CommunionTheme(gathered = ref("john", 1, 1, 3), offerings = listOf(ref("john", 13, 34, 35))),
            CommunionTheme(gathered = ref("hebrews", 11, 1, 1), offerings = listOf(ref("hebrews", 4, 12, 12))),
            CommunionTheme(gathered = ref("proverbs", 3, 5, 6), offerings = listOf(ref("john", 11, 25, 26))),
        ),
    )

    private val generator = CommunionGenerator(seedManifest, seed)

    @Test
    fun communionSeedFirstDay() {
        val day = generator.communionForDate("2026-06-01")
        assertEquals("john", day.gathered.book)
        assertEquals(1, day.gathered.chapter)
        assertEquals(1, day.gathered.startVerse)
        assertEquals(3, day.gathered.endVerse)
    }

    @Test
    fun communionSeedCycles() {
        // Day 0 = theme 0, Day 3 = theme 0 again (3 themes × 7 days = cycle)
        val day0 = generator.communionForDate("2026-06-01")
        val day3 = generator.communionForDate("2026-06-04")
        assertEquals(day0.gathered.book, day3.gathered.book)
        assertEquals(day0.gathered.chapter, day3.gathered.chapter)
    }
}
