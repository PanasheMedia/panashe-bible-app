package org.panashe.bible.features.communion

import org.panashe.bible.BibleData
import org.panashe.bible.buildCommunionView
import org.panashe.bible.loadBundledBibleData

/**
 * Source of the daily Communion view. The UI depends only on this interface, so
 * the data source can change without touching screens.
 *
 * Today this is backed by the bundled proof-of-concept seed
 * ([StaticCommunionRepository]). Once submitted offerings are stored in a
 * database, a RemoteCommunionRepository (Cloudflare D1 via Ktor) can implement
 * the same contract and drop in with no UI changes.
 */
interface CommunionRepository {
    /** The Communion view for today (reading + kept Communion). */
    suspend fun todayView(): CommunionView
}

/**
 * Builds the Communion view from the bundled seed and shared rules. The loaded
 * corpus is cached after the first call so repeated reads are cheap.
 */
class StaticCommunionRepository(
    private val dataProvider: suspend () -> BibleData = { loadBundledBibleData() },
) : CommunionRepository {
    private var cached: BibleData? = null

    override suspend fun todayView(): CommunionView {
        val data = cached ?: dataProvider().also { cached = it }
        return buildCommunionView(data)
    }
}
