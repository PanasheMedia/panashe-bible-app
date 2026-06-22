package org.panashe.bible.features.communion

import org.panashe.bible.BibleData
import org.panashe.bible.network.CommunionApi
import org.panashe.bible.network.OfferRequest

/**
 * Communion source backed by the shared API (Cloudflare Worker + D1).
 *
 * Reads delegate to [delegate] (the bundled seed): today's reading and kept
 * Communion are computed locally from the shared rules, which produce the same
 * result the server does — so reads stay instant and work offline. The API is
 * used for the write path: submitting reader offerings to the shared backend.
 */
class RemoteCommunionRepository(
    private val clientId: String,
    private val delegate: CommunionRepository = StaticCommunionRepository(),
    private val api: CommunionApi = CommunionApi(),
) : CommunionRepository {

    override suspend fun todayView(): CommunionView = delegate.todayView()

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
}
