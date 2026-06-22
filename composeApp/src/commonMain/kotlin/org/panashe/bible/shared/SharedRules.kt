// GENERATED FILE - DO NOT EDIT.
//
// Generated from panashe-bible-shared by scripts/generate-kotlin-rules.mjs.
// Update the shared package, then run: npm run export:app
//
// Constants are single-sourced from panashe-bible-shared/src/constants.js.
// Behaviour mirrors src/reference.js, src/communion.js, and src/routes.js and is
// verified by the shared conformance fixtures.

package org.panashe.bible.shared

object SharedConstants {
    const val DEFAULT_TRANSLATION = "kjva"

    const val SECTION_OLD_TESTAMENT = "Old Testament"
    const val SECTION_APOCRYPHA = "Apocrypha"
    const val SECTION_NEW_TESTAMENT = "New Testament"

    const val COMMUNION_START_ISO = "2026-06-01"
    const val COMMUNION_TIMEZONE = "UTC"
    const val OFFERING_MIN_VERSES = 1
    const val OFFERING_MAX_VERSES = 3
    const val READING_LENGTH = 3
    const val COMMON_WITNESS_COUNT = 3
    const val HIDDEN_WITNESS_COUNT = 2
    const val COMMUNION_THREAD_COUNT = 7
    const val KEPT_BENEATH_COUNT = COMMUNION_THREAD_COUNT - 1

    const val ROUTE_DAILY = "/"
    const val ROUTE_BIBLE = "/bible"
    const val ROUTE_COMMUNION = "/bible/church"
    const val ROUTE_ARCHIVE = "/archive"
    const val ROUTE_ABOUT = "/about"
    const val ROUTE_PRIVACY = "/privacy"
}

/** Canonical Scripture reference, mirroring scripture-reference.schema.json. */
data class ScriptureReference(
    val translation: String,
    val book: String,
    val chapter: Int,
    val startVerse: Int,
    val endVerse: Int,
)

object SharedRules {
    /**
     * Normalizes raw reference parts into a canonical reference and validates it.
     * Mirrors canonicalReference + validateReference in src/reference.js.
     */
    fun canonicalReference(
        book: String,
        chapter: Int,
        startVerse: Int,
        endVerse: Int = startVerse,
        translation: String = SharedConstants.DEFAULT_TRANSLATION,
    ): ScriptureReference {
        val reference = ScriptureReference(
            translation = normalizeSlug(translation.ifBlank { SharedConstants.DEFAULT_TRANSLATION }),
            book = normalizeSlug(book),
            chapter = chapter,
            startVerse = startVerse,
            endVerse = endVerse,
        )
        validateReference(reference)
        return reference
    }

    /** Mirrors validateReference in src/reference.js. */
    fun validateReference(reference: ScriptureReference) {
        require(reference.translation.isNotBlank()) { "Missing Scripture reference field: translation" }
        require(reference.book.isNotBlank()) { "Missing Scripture reference field: book" }
        require(reference.chapter >= 1) { "Scripture reference chapter must be a positive integer." }
        require(reference.startVerse >= 1) { "Scripture reference startVerse must be a positive integer." }
        require(reference.endVerse >= reference.startVerse) {
            "Scripture reference endVerse must be greater than or equal to startVerse."
        }
    }

    /** Mirrors referenceVerseCount in src/reference.js. */
    fun referenceVerseCount(reference: ScriptureReference): Int {
        validateReference(reference)
        return reference.endVerse - reference.startVerse + 1
    }

    /** Mirrors referenceKey in src/reference.js. */
    fun referenceKey(reference: ScriptureReference): String =
        listOf(
            reference.translation,
            reference.book,
            reference.chapter,
            reference.startVerse,
            reference.endVerse,
        ).joinToString(":")

    /**
     * Formats a reference for display, e.g. "John 1:1-3".
     * Mirrors formatReference in src/reference.js. [bookName] should be the
     * display name from the manifest; if null the slug is used.
     */
    fun formatReference(reference: ScriptureReference, bookName: String? = null): String {
        val name = bookName ?: reference.book
        val versePart = if (reference.startVerse == reference.endVerse) {
            "${reference.startVerse}"
        } else {
            "${reference.startVerse}-${reference.endVerse}"
        }
        return "$name ${reference.chapter}:$versePart"
    }

    /** True when an offering is within the allowed verse window. */
    fun isValidOffering(reference: ScriptureReference): Boolean {
        validateReference(reference)
        val count = referenceVerseCount(reference)
        return count in SharedConstants.OFFERING_MIN_VERSES..SharedConstants.OFFERING_MAX_VERSES
    }

    /**
     * Validates a Communion offering, throwing when out of range.
     * Mirrors validateCommunionOffering in src/communion.js.
     */
    fun validateCommunionOffering(reference: ScriptureReference): ScriptureReference {
        require(isValidOffering(reference)) {
            "Communion offerings must be ${SharedConstants.OFFERING_MIN_VERSES}-${SharedConstants.OFFERING_MAX_VERSES} verses."
        }
        return reference
    }

    /** Mirrors chapterPath in src/routes.js. */
    fun chapterPath(reference: ScriptureReference): String =
        listOf(
            SharedConstants.ROUTE_BIBLE,
            reference.translation.ifBlank { SharedConstants.DEFAULT_TRANSLATION },
            reference.book,
            reference.chapter.toString(),
        ).joinToString("/")

    /** Mirrors versePath in src/routes.js. */
    fun versePath(reference: ScriptureReference): String =
        "${chapterPath(reference)}/verse-${reference.startVerse}"

    /** Mirrors communionArchivePath in src/routes.js. */
    fun communionArchivePath(date: String): String =
        "${SharedConstants.ROUTE_ARCHIVE}/$date"

    private fun normalizeSlug(value: String): String {
        val trimmed = value.trim()
        require(trimmed.isNotEmpty()) { "Scripture reference slugs must be non-empty strings." }
        return trimmed.lowercase()
    }
}
