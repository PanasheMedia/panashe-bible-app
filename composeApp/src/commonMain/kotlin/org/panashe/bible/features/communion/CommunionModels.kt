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
