package org.panashe.bible.features.communion

import org.panashe.bible.BibleData
import org.panashe.bible.features.reader.DailyReading
import org.panashe.bible.network.CommunionApi
import org.panashe.bible.network.CommunionResponse
import org.panashe.bible.network.OfferRequest
import org.panashe.bible.network.WireRef
import org.panashe.bible.shared.ScriptureReference
import org.panashe.bible.shared.SharedConstants
import org.panashe.bible.shared.SharedRules

/**
 * Communion source backed by the shared API (Cloudflare Worker + D1).
 *
 * Today's reading and the gathered witness (Common + Hidden) are fetched live
 * from the server, so every reader sees the same thing the web does — gathered
 * from real offerings, with counts hidden. Verse *text* is still resolved from
 * the bundled corpus. If the API is unreachable, reads fall back to [delegate]
 * (the bundled seed) so the screen always has something to show.
 */
class RemoteCommunionRepository(
    private val clientId: String,
    private val delegate: CommunionRepository = StaticCommunionRepository(),
    private val api: CommunionApi = CommunionApi(),
) : CommunionRepository {

    override suspend fun todayView(): CommunionView {
        val data = delegate.bibleData()
        return try {
            val response = api.getCommunion(CommunionGenerator.todayIso())
            buildViewFromApi(data, response)
        } catch (_: Exception) {
            delegate.todayView() // offline → bundled seed
        }
    }

    override suspend fun bibleData(): BibleData = delegate.bibleData()

    override suspend fun submitOffering(
        dateIso: String,
        bookSlug: String,
        chapter: Int,
        startVerse: Int,
        endVerse: Int,
    ) {
        api.submitOffering(
            OfferRequest(
                date = dateIso,
                book = bookSlug,
                chapter = chapter,
                startVerse = startVerse,
                endVerse = endVerse,
                clientId = clientId,
            )
        )
    }

    private suspend fun buildViewFromApi(data: BibleData, response: CommunionResponse): CommunionView {
        suspend fun entryFromReference(reference: ScriptureReference): CommunionEntry {
            return CommunionEntry(
                reference = reference,
                display = data.displayReference(reference),
                preview = data.passageText(reference),
                state = "",
            )
        }

        suspend fun entryFromWire(wire: WireRef): CommunionEntry {
            return entryFromReference(wire.toReference())
        }

        val readingRef = response.reading.toReference()
        val readingBook = data.book(response.reading.slug)
        val readingChapter = readingBook.chapter(response.reading.chapter)
        val reading = DailyReading(
            dateLabel = response.dateLabel,
            reference = readingRef,
            display = data.displayReference(readingRef),
            chapterTitle = "${readingBook.name} ${response.reading.chapter}",
            chapterIntro = readingChapter?.introduction ?: readingBook.introduction,
            verses = readingChapter?.verseRange(response.reading.start, response.reading.end).orEmpty(),
            chapterVerses = readingChapter?.verses.orEmpty(),
        )

        // The Communion thread: the verses interacting today, most-offered first.
        val seedRefs = CommunionGenerator(data.manifest, data.seed).communionForDate(response.date).offerings
        val liveRefs = response.communion.map { it.toReference() }
        val threadRefs = (liveRefs + seedRefs)
            .distinctBy { SharedRules.referenceKey(it) }
            .take(SharedConstants.COMMUNION_THREAD_COUNT)
        val thread = threadRefs.map { entryFromReference(it) }
        val gathered = thread.firstOrNull() ?: entryFromWire(response.reading)
        return CommunionView(
            reading = reading,
            kept = KeptCommunion(
                date = response.dateLabel,
                gathered = gathered,
                beneath = thread.drop(1),
            ),
        )
    }
}

private fun WireRef.toReference() = ScriptureReference(
    translation = "kjva",
    book = slug,
    chapter = chapter,
    startVerse = start,
    endVerse = end,
)
