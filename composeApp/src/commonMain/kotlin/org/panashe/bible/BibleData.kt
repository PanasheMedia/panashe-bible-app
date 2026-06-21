package org.panashe.bible

import org.jetbrains.compose.resources.ExperimentalResourceApi
import panashebible.composeapp.generated.resources.Res

data class BibleManifest(
    val translation: TranslationInfo,
    val books: List<BookSummary>
)

data class TranslationInfo(
    val abbreviation: String,
    val slug: String,
    val name: String
)

data class BookSummary(
    val name: String,
    val slug: String,
    val chapters: Int,
    val section: String
)

data class BibleBook(
    val name: String,
    val slug: String,
    val introduction: String,
    val chapters: List<BibleChapter>
)

data class BibleChapter(
    val number: Int,
    val title: String,
    val introduction: String,
    val verses: List<BibleVerse>
)

data class BibleVerse(
    val number: Int,
    val text: String
)

data class BibleData(
    val manifest: BibleManifest,
    val john: BibleBook
) {
    val johnOne: BibleChapter = john.chapters.first { it.number == 1 }
}

@OptIn(ExperimentalResourceApi::class)
suspend fun loadBundledBibleData(): BibleData {
    val manifestJson = Res.readBytes("files/bible/manifest.json").decodeToString()
    val johnJson = Res.readBytes("files/bible/books/john.json").decodeToString()

    return BibleData(
        manifest = parseManifest(manifestJson),
        john = parseBook(johnJson)
    )
}

private fun parseManifest(json: String): BibleManifest {
    val translationBlock = json.substringAfter("\"translation\":{").substringBefore("},\"books\"")
    val booksBlock = json.substringAfter("\"books\":[").substringBeforeLast("]}")
    val bookObjects = Regex("""\{"index":\d+,"name":"(.*?)","slug":"(.*?)","chapters":(\d+),"section":"(.*?)","description":"(.*?)"}""")
        .findAll(booksBlock)
        .map {
            BookSummary(
                name = unescapeJson(it.groupValues[1]),
                slug = it.groupValues[2],
                chapters = it.groupValues[3].toInt(),
                section = unescapeJson(it.groupValues[4])
            )
        }
        .toList()

    return BibleManifest(
        translation = TranslationInfo(
            abbreviation = jsonValue(translationBlock, "abbreviation"),
            slug = jsonValue(translationBlock, "slug"),
            name = jsonValue(translationBlock, "name")
        ),
        books = bookObjects
    )
}

private fun parseBook(json: String): BibleBook {
    val name = jsonValue(json, "name")
    val slug = jsonValue(json, "slug")
    val introduction = jsonValue(json, "introduction")
    val chapterMatches = Regex("""\{"title":"(.*?)","introduction":"(.*?)","chapter":(\d+),"verses":\[(.*?)]}""")
        .findAll(json)
        .map {
            BibleChapter(
                title = unescapeJson(it.groupValues[1]),
                introduction = unescapeJson(it.groupValues[2]),
                number = it.groupValues[3].toInt(),
                verses = parseVerses(it.groupValues[4])
            )
        }
        .toList()

    return BibleBook(
        name = name,
        slug = slug,
        introduction = introduction,
        chapters = chapterMatches
    )
}

private fun parseVerses(json: String): List<BibleVerse> {
    return Regex("""\{"verse":(\d+),"text":"(.*?)"}""")
        .findAll(json)
        .map {
            BibleVerse(
                number = it.groupValues[1].toInt(),
                text = unescapeJson(it.groupValues[2])
            )
        }
        .toList()
}

private fun jsonValue(json: String, key: String): String {
    val match = Regex(""""$key":"(.*?)"""").find(json)
        ?: error("Missing JSON key: $key")
    return unescapeJson(match.groupValues[1])
}

private fun unescapeJson(value: String): String {
    return value
        .replace("\\\"", "\"")
        .replace("\\\\", "\\")
        .replace("\\n", "\n")
}
