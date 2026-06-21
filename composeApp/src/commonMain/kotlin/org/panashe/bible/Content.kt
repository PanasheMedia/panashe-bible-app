package org.panashe.bible

enum class PanasheRoute(val path: String, val title: String) {
    Daily("/", "Daily"),
    Bible("/bible", "Scripture"),
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

data class KeptCommunion(
    val date: String,
    val gathered: CommunionEntry,
    val beneath: List<CommunionEntry>
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

val todayCommunion = KeptCommunion(
    date = "June 1, 2026",
    gathered = CommunionEntry(
        reference = "John 1:1-3",
        preview = "In the beginning was the Word, and the Word was with God, and the Word was God. The same was in the beginning with God. All things were made by him; and without him was not any thing made that was made.",
        state = "Gathered passage"
    ),
    beneath = listOf(
        CommunionEntry(
            reference = "John 13:34-35",
            preview = "A new commandment I give unto you, That ye love one another; as I have loved you...",
            state = "Kept beneath"
        ),
        CommunionEntry(
            reference = "Romans 12:10",
            preview = "Be kindly affectioned one to another with brotherly love; in honour preferring one another;",
            state = "Kept beneath"
        ),
        CommunionEntry(
            reference = "I Peter 1:22",
            preview = "Seeing ye have purified your souls in obeying the truth through the Spirit unto unfeigned love of the brethren...",
            state = "Kept beneath"
        ),
        CommunionEntry(
            reference = "Colossians 3:14",
            preview = "And above all these things put on charity, which is the bond of perfectness.",
            state = "Kept beneath"
        ),
        CommunionEntry(
            reference = "Psalms 133:1",
            preview = "Behold, how good and how pleasant it is for brethren to dwell together in unity!",
            state = "Kept beneath"
        ),
        CommunionEntry(
            reference = "I John 4:7-8",
            preview = "Beloved, let us love one another: for love is of God...",
            state = "Kept beneath"
        )
    )
)

val aboutParagraphs = listOf(
    "Panashe speaks of the presence of God. The project is shaped to help readers dwell with Scripture, not rush past it.",
    "Daily Communion has three surfaces: Today's Reading, Today's Offering, and Today's Communion.",
    "Readers offer complete Scripture references. Matching references form a Common Witness; quieter offerings remain part of the Hidden Witness.",
    "The kept Communion is the final seven: one gathered passage with six beneath it, shown without names, counts, comments, or rankings."
)

val privacyParagraphs = listOf(
    "Reading preferences stay on the device.",
    "Communion offerings are received without public identity. Counts and non-kept offerings are not shown publicly.",
    "Cloudflare may process basic request data to serve the site and protect it from abuse."
)
