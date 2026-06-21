package org.panashe.bible.features.communion

import org.panashe.bible.shared.ScriptureReference
import org.panashe.bible.features.reader.DailyReading

/** A Communion entry with its display reference and the resolved Scripture text. */
data class CommunionEntry(
    val reference: ScriptureReference,
    val display: String,
    val preview: String,
    val state: String
)

/** The kept seven: one gathered passage with beneath offerings. */
data class KeptCommunion(
    val date: String,
    val gathered: CommunionEntry,
    val beneath: List<CommunionEntry>
)

/** Everything the screens need, derived from bundled data + shared rules. */
data class CommunionView(
    val reading: DailyReading,
    val kept: KeptCommunion
)

/** A single archive day entry for the archive list. */
data class ArchiveDay(
    val iso: String,
    val dateLabel: String,
    val reference: String,
    val offerings: List<org.panashe.bible.shared.ScriptureReference>
)

/** Full detail of an archive day for the dialog. */
data class ArchiveDetail(
    val dateLabel: String,
    val gatheredRef: String,
    val gatheredText: String,
    val offerings: List<ArchiveDetailEntry>
)

data class ArchiveDetailEntry(val reference: String, val text: String)
