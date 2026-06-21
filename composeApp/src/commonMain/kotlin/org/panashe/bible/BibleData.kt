package org.panashe.bible

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.panashe.bible.shared.ScriptureReference
import org.panashe.bible.shared.SharedConstants
import org.panashe.bible.shared.SharedRules
import panashebible.composeapp.generated.resources.Res

// Bundled Bible data is produced by panashe-bible-shared (npm run export:app).
// These models mirror the shared JSON schemas. Do not author Scripture here.

@Serializable
data class BibleManifest(
    val translation: TranslationInfo,
    val books: List<BookSummary>
) {
    fun bookName(slug: String): String? = books.firstOrNull { it.slug == slug }?.name
}

@Serializable
data class TranslationInfo(
    val abbreviation: String,
    val slug: String,
    val name: String,
    val attribution: String = ""
)

@Serializable
data class BookSummary(
    val index: Int = 0,
    val name: String,
    val slug: String,
    val chapters: Int,
    val section: String,
    val description: String = ""
)

@Serializable
data class BibleBook(
    val name: String,
    val slug: String,
    val introduction: String,
    val chapters: List<BibleChapter>
) {
    fun chapter(number: Int): BibleChapter? = chapters.firstOrNull { it.number == number }
}

@Serializable
data class BibleChapter(
    val title: String,
    val introduction: String,
    @SerialName("chapter") val number: Int,
    val verses: List<BibleVerse>
) {
    fun verseRange(start: Int, end: Int): List<BibleVerse> =
        verses.filter { it.number in start..end }
}

@Serializable
data class BibleVerse(
    @SerialName("verse") val number: Int,
    val text: String
)

/**
 * Loaded Scripture corpus. Books are loaded lazily on demand to keep startup
 * cheap; the manifest and the seed are loaded eagerly.
 */
class BibleData(
    val manifest: BibleManifest,
    val seed: CommunionSeed,
    val searchIndex: List<SearchIndexEntry>,
    private val bookLoader: suspend (String) -> BibleBook
) {
    private val cache = mutableMapOf<String, BibleBook>()

    suspend fun book(slug: String): BibleBook {
        cache[slug]?.let { return it }
        val loaded = bookLoader(slug)
        cache[slug] = loaded
        return loaded
    }

    /** Resolves the verse text for a reference, joining multi-verse passages. */
    suspend fun passageText(reference: ScriptureReference): String {
        val loaded = book(reference.book)
        val chapter = loaded.chapter(reference.chapter) ?: return ""
        return chapter.verseRange(reference.startVerse, reference.endVerse)
            .joinToString(" ") { it.text }
    }

    fun displayReference(reference: ScriptureReference): String =
        SharedRules.formatReference(reference, manifest.bookName(reference.book))

    fun search(query: String, limit: Int = 50): List<SearchIndexEntry> {
        if (query.isBlank()) return emptyList()
        val lower = query.lowercase()
        return searchIndex.filter { it.text.lowercase().contains(lower) }.take(limit)
    }
}

@Serializable
data class SearchIndexEntry(
    @SerialName("b") val book: String,
    @SerialName("c") val chapter: Int,
    @SerialName("v") val verse: Int,
    @SerialName("t") val text: String
)

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

@OptIn(ExperimentalResourceApi::class)
suspend fun loadBundledBibleData(): BibleData {
    val manifestJson = Res.readBytes("files/bible/manifest.json").decodeToString()
    val seedJson = Res.readBytes("files/bible/communion-seed.json").decodeToString()
    val searchIndexJson = Res.readBytes("files/bible/search-index.json").decodeToString()

    val manifest = json.decodeFromString<BibleManifest>(manifestJson)
    val seed = parseCommunionSeed(seedJson)
    val searchIndex = json.decodeFromString<List<SearchIndexEntry>>(searchIndexJson)

    return BibleData(
        manifest = manifest,
        seed = seed,
        searchIndex = searchIndex,
        bookLoader = { slug ->
            val bookJson = Res.readBytes("files/bible/books/$slug.json").decodeToString()
            json.decodeFromString<BibleBook>(bookJson)
        }
    )
}

// --- Communion seed -------------------------------------------------------
//
// The seed encodes references as mixed-type arrays:
//   [bookSlug, chapter, startVerse, endVerse]
// so it is parsed via JsonElement rather than typed serialization.

class CommunionSeed(
    val startIso: String,
    val endIso: String,
    val themes: List<CommunionTheme>
) {
    /** The kept Communion for the start date (the first theme). */
    val first: CommunionTheme get() = themes.first()
}

class CommunionTheme(
    val gathered: ScriptureReference,
    val offerings: List<ScriptureReference>
)

private fun referenceFromArray(array: JsonArray): ScriptureReference {
    val book = array[0].jsonPrimitive.content
    val chapter = array[1].jsonPrimitive.int
    val startVerse = array[2].jsonPrimitive.int
    val endVerse = array[3].jsonPrimitive.int
    return SharedRules.canonicalReference(
        book = book,
        chapter = chapter,
        startVerse = startVerse,
        endVerse = endVerse,
    )
}

fun parseCommunionSeed(seedJson: String): CommunionSeed {
    val root = json.parseToJsonElement(seedJson).jsonObject
    val startIso = root["startIso"]?.jsonPrimitive?.content ?: SharedConstants.COMMUNION_START_ISO
    val endIso = root["endIso"]?.jsonPrimitive?.content ?: startIso
    val themeElements = root["themes"]?.jsonArray ?: JsonArray(emptyList())
    val themes = themeElements.map { themeElement ->
        val theme = themeElement.jsonObject
        val gathered = referenceFromArray(theme["gathered"]!!.jsonArray)
        val offerings = theme["offerings"]!!.jsonArray.map { referenceFromArray(it.jsonArray) }
        CommunionTheme(gathered = gathered, offerings = offerings)
    }
    return CommunionSeed(startIso = startIso, endIso = endIso, themes = themes)
}
