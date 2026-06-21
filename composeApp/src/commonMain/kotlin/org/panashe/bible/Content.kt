package org.panashe.bible

enum class PanasheRoute(val path: String, val title: String) {
    Daily("/", "Daily Reading"),
    Bible("/bible", "Bible"),
    Communion("/bible/church", "Communion"),
    About("/about", "About"),
    Privacy("/privacy", "Privacy")
}

data class ScriptureReading(
    val label: String,
    val reference: String,
    val intro: String,
    val verses: List<String>,
    val contextTitle: String,
    val context: String
)

data class CommunionEntry(
    val reference: String,
    val preview: String,
    val state: String
)

val todayReading = ScriptureReading(
    label = "Today's Reading",
    reference = "John 1:1-3",
    intro = "A passage for today, drawn from the opening witness of John.",
    verses = listOf(
        "In the beginning was the Word, and the Word was with God, and the Word was God.",
        "The same was in the beginning with God.",
        "All things were made by him; and without him was not any thing made that was made."
    ),
    contextTitle = "Chapter Context",
    context = "John 1 opens with the Word before creation, drawing the reader back to Christ as the beginning and the life of the world."
)

val communionEntries = listOf(
    CommunionEntry(
        reference = "John 1:1-3",
        preview = "In the beginning was the Word, and the Word was with God, and the Word was God.",
        state = "Kept from yesterday"
    ),
    CommunionEntry(
        reference = "Psalm 33:6-7",
        preview = "By the word of the Lord were the heavens made; and all the host of them by the breath of his mouth.",
        state = "Kept two days ago"
    ),
    CommunionEntry(
        reference = "Hebrews 1:1-3",
        preview = "God, who at sundry times and in divers manners spake in time past unto the fathers by the prophets...",
        state = "Kept three days ago"
    )
)

val aboutParagraphs = listOf(
    "Panashe speaks of the presence of God. The project is shaped to help readers dwell with Scripture, not rush past it.",
    "The church is the assembly called together in Christ, so Communion is named as a gathering rather than a feed.",
    "The aim is simple: read, remember, pray, and discover how the Bible answers the Bible."
)

val privacyParagraphs = listOf(
    "Reading preferences stay on the device.",
    "Suggestions and future community features are intended to be anonymous and minimally collected.",
    "Cloudflare may process basic request data to serve the site and protect it from abuse."
)

