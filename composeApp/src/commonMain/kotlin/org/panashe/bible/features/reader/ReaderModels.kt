package org.panashe.bible.features.reader

import org.panashe.bible.BibleVerse
import org.panashe.bible.shared.ScriptureReference

/** Today's reading derived from the seed's gathered passage. */
data class DailyReading(
    val dateLabel: String,
    val reference: ScriptureReference,
    val display: String,
    val chapterTitle: String,
    val chapterIntro: String,
    val verses: List<BibleVerse>,
    val chapterVerses: List<BibleVerse>
)
