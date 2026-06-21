package org.panashe.bible

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF262624)
private val Muted = Color(0xFF77756F)
private val Paper = Color(0xFFF8F7F3)
private val SurfaceColor = Color(0xFFFFFFFF)
private val Soft = Color(0xFFEFECE5)
private val Line = Color(0xFFDFDDD6)
private val Accent = Color(0xFFC64F35)

@Composable
fun PanasheApp() {
    var route by remember { mutableStateOf(PanasheRoute.Daily) }
    var view by remember { mutableStateOf<CommunionView?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        runCatching {
            val data = loadBundledBibleData()
            buildCommunionView(data)
        }
            .onSuccess { view = it }
            .onFailure { loadError = it.message ?: "Unable to load bundled Bible data." }
    }

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = Ink,
            secondary = Accent,
            surface = Paper,
            onSurface = Ink
        ),
        typography = MaterialTheme.typography.copy(
            displayLarge = MaterialTheme.typography.displayLarge.copy(fontFamily = FontFamily.Serif),
            displayMedium = MaterialTheme.typography.displayMedium.copy(fontFamily = FontFamily.Serif),
            headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Serif),
            titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif),
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Serif)
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Paper) {
            Column(modifier = Modifier.fillMaxSize()) {
                Header(route = route, onRouteChange = { route = it })
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier.widthIn(max = 820.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        when (route) {
                            PanasheRoute.Daily -> DailyReadingScreen(
                                view = view,
                                loadError = loadError,
                                onBible = { route = PanasheRoute.Bible }
                            )
                            PanasheRoute.Bible -> BibleScreen(view = view, loadError = loadError)
                            PanasheRoute.Communion -> CommunionScreen(view = view)
                            PanasheRoute.About -> TextPage("About", aboutParagraphs)
                            PanasheRoute.Privacy -> TextPage("Privacy Policy", privacyParagraphs)
                        }
                        Footer(onRouteChange = { route = it })
                    }
                }
                BottomTabs(route = route, onRouteChange = { route = it })
            }
        }
    }
}

@Composable
private fun Header(route: PanasheRoute, onRouteChange: (PanasheRoute) -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth().background(Paper).border(BorderStroke(0.5.dp, Line))) {
        val showPrimaryTabs = maxWidth > 700.dp
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "The Bible",
                color = Ink,
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            if (showPrimaryTabs) {
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    PrimaryTab("Daily", route == PanasheRoute.Daily) { onRouteChange(PanasheRoute.Daily) }
                    PrimaryTab("Scripture", route == PanasheRoute.Bible) { onRouteChange(PanasheRoute.Bible) }
                    PrimaryTab("Communion", route == PanasheRoute.Communion) { onRouteChange(PanasheRoute.Communion) }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    HeaderAction("Aa")
                    HeaderAction("Search")
                }
            }
        }
    }
}

@Composable
private fun PrimaryTab(label: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick, shape = RoundedCornerShape(0.dp)) {
        Text(label, color = if (selected) Ink else Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun HeaderAction(label: String) {
    TextButton(onClick = {}, shape = RoundedCornerShape(50)) {
        Text(label, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DailyReadingScreen(view: CommunionView?, loadError: String?, onBible: () -> Unit) {
    val reading = view?.reading
    Hero(
        eyebrow = reading?.dateLabel ?: "Daily",
        title = "Daily Reading",
        intro = "A daily portion of Holy Scripture for reading, remembrance, and obedience before God."
    )

    SectionCard {
        Eyebrow("Today's Reading")
        Spacer(Modifier.height(10.dp))
        Text(
            reading?.display ?: "Loading...",
            color = Ink,
            fontFamily = FontFamily.Serif,
            fontSize = 34.sp,
            lineHeight = 38.sp
        )
        Spacer(Modifier.height(16.dp))
        when {
            loadError != null -> LoadingText(loadError)
            reading == null -> LoadingText("Loading Scripture...")
            else -> reading.verses.forEach { verse ->
                Text(
                    text = verse.text,
                    color = Ink,
                    fontFamily = FontFamily.Serif,
                    fontSize = 20.sp,
                    lineHeight = 36.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }
    }

    SectionCard {
        Eyebrow("Chapter Context")
        Spacer(Modifier.height(10.dp))
        Text(reading?.chapterTitle ?: "Chapter", color = Ink, fontFamily = FontFamily.Serif, fontSize = 30.sp)
        Spacer(Modifier.height(10.dp))
        Text(reading?.chapterIntro ?: "The chapter context will appear when bundled Scripture finishes loading.", color = Muted, lineHeight = 24.sp)
        Spacer(Modifier.height(16.dp))
        FlowRow(horizontalArrangement = Arrangement.Center, verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            PrimaryAction("Read full chapter") {}
            SecondaryAction("Copy passage") {}
            SecondaryAction("Browse the Bible", onBible)
        }
    }
}

@Composable
private fun BibleScreen(view: CommunionView?, loadError: String?) {
    val reading = view?.reading
    ReaderToolbar(reading)
    SectionCard {
        Eyebrow(reading?.chapterTitle ?: "Scripture")
        Spacer(Modifier.height(10.dp))
        Text(reading?.chapterTitle ?: "Bible", color = Ink, fontFamily = FontFamily.Serif, fontSize = 52.sp, lineHeight = 56.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        Text(
            reading?.chapterIntro ?: "Loading the bundled Scripture text.",
            color = Muted,
            fontSize = 13.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(26.dp))
        when {
            loadError != null -> LoadingText(loadError)
            reading == null -> LoadingText("Loading Scripture...")
            else -> reading.chapterVerses.forEach { verse ->
                Text(
                    text = "${verse.number}  ${verse.text}",
                    color = Ink,
                    fontFamily = FontFamily.Serif,
                    fontSize = 20.sp,
                    lineHeight = 36.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun CommunionScreen(view: CommunionView?) {
    var hasOffered by remember { mutableStateOf(false) }
    var selectedTopTab by remember { mutableStateOf("read") }
    var selectedKeptTab by remember { mutableStateOf("today") }

    val reading = view?.reading
    val kept = view?.kept

    CommunionHero()

    SectionCard {
        SegmentedTabs(
            first = "Read Today's 3 Verses",
            second = "Offer One Reference",
            firstSelected = selectedTopTab == "read",
            onFirst = { selectedTopTab = "read" },
            onSecond = { selectedTopTab = "offer" }
        )
        Spacer(Modifier.height(18.dp))
        if (selectedTopTab == "read") {
            Eyebrow(reading?.dateLabel ?: "Today")
            Text(reading?.display ?: "Loading...", color = Ink, fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(14.dp))
            Text(reading?.chapterIntro ?: "Loading the chapter context from bundled Scripture.", color = Muted, fontSize = 14.sp, lineHeight = 24.sp)
            Spacer(Modifier.height(14.dp))
            PrimaryAction("Read the full chapter") {}
        } else {
            Text("Offer One Reference", color = Ink, fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            Text(
                "Choose a book, chapter, and 1 to 3 consecutive verses that connect with today's passage. You can offer once per day.",
                color = Muted,
                fontSize = 14.sp,
                lineHeight = 24.sp
            )
            ProcessNote()
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ReferencePill("Book")
                ReferencePill("Chapter")
                ReferencePill("Start")
                ReferencePill("End")
            }
            Spacer(Modifier.height(14.dp))
            Text("After you offer, this form closes for today. The kept Communion is assembled privately from all offerings.", color = Muted, fontSize = 13.sp, lineHeight = 21.sp)
            Spacer(Modifier.height(14.dp))
            if (hasOffered) {
                Text("Your offering has been received for today.", color = Accent, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp)
            } else {
                PrimaryAction("Submit today's offering") { hasOffered = true }
            }
        }
    }

    SectionCard {
        SegmentedTabs(
            first = "View Kept Seven",
            second = "Previous Kept Seven",
            firstSelected = selectedKeptTab == "today",
            onFirst = { selectedKeptTab = "today" },
            onSecond = { selectedKeptTab = "previous" }
        )
        Spacer(Modifier.height(20.dp))
        if (kept == null) {
            LoadingText("Loading the kept Communion...")
        } else {
            KeptCommunionContent(kept, if (selectedKeptTab == "today") "Today" else "Previous")
        }
    }

    SectionCard {
        Text("Archive", color = Ink, fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        Text(
            "Previous gathered Communions, preserved. Tap any entry to see the final Scripture shown without counts, names, or rankings.",
            color = Muted,
            fontSize = 14.sp,
            lineHeight = 24.sp
        )
        Spacer(Modifier.height(14.dp))
        if (kept != null) {
            ArchiveRow(kept.date, kept.gathered.display)
        }
    }
}

@Composable
private fun LoadingText(text: String) {
    Text(text, color = Muted, fontSize = 14.sp, lineHeight = 24.sp)
}

@Composable
private fun Hero(eyebrow: String?, title: String, intro: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 54.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (eyebrow != null) {
            Eyebrow(eyebrow)
            Spacer(Modifier.height(6.dp))
        }
        Text(
            title,
            color = Ink,
            fontFamily = FontFamily.Serif,
            fontSize = 52.sp,
            lineHeight = 54.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(14.dp))
        Text(intro, color = Muted, fontSize = 15.sp, lineHeight = 25.sp, textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 480.dp))
    }
}

@Composable
private fun ReaderToolbar(reading: DailyReading?) {
    val bookName = reading?.chapterTitle?.substringBeforeLast(" ") ?: "Book"
    val chapterNumber = reading?.reference?.chapter?.toString() ?: "1"
    Row(modifier = Modifier.fillMaxWidth().border(BorderStroke(1.dp, Line)), verticalAlignment = Alignment.CenterVertically) {
        ToolbarSelector("Book", bookName, Modifier.weight(1f))
        ToolbarSelector("Chapter", chapterNumber, Modifier.weight(1f))
        ToolbarSelector("Translation", "KJVA", Modifier.weight(1f))
    }
}

@Composable
private fun ToolbarSelector(label: String, value: String, modifier: Modifier) {
    Column(modifier = modifier.padding(14.dp)) {
        Eyebrow(label)
        Text(value, color = Ink, fontFamily = FontFamily.Serif, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CommunionHero() {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 54.dp, bottom = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("+", color = Accent, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        Text("Daily Communion", color = Ink, fontFamily = FontFamily.Serif, fontSize = 52.sp, lineHeight = 54.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(14.dp))
        Text(
            "Daily Communion has three parts: Today's Reading, Today's Offering, and Today's Communion. Readers offer Scripture references; matching references form a Common Witness, while quieter offerings remain part of the Hidden Witness.",
            color = Muted,
            fontSize = 15.sp,
            lineHeight = 25.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 560.dp)
        )
        Spacer(Modifier.height(18.dp))
        FlowRow(horizontalArrangement = Arrangement.Center, verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            CommunionStep("1", "Read today's 3 verses")
            CommunionStep("2", "Offer one reference")
            CommunionStep("3", "View the kept seven")
        }
        Spacer(Modifier.height(12.dp))
        Text("SCRIPTURE ONLY. NO PROFILES, COMMENTS, SCORES, OR RANKINGS.", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
    }
}

@Composable
private fun CommunionStep(number: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 8.dp)) {
        Box(modifier = Modifier.size(22.dp).background(Color(0x1FC64F35), RoundedCornerShape(50)), contentAlignment = Alignment.Center) {
            Text(number, color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Text(label, color = Muted, fontSize = 13.sp)
    }
}

@Composable
private fun SegmentedTabs(first: String, second: String, firstSelected: Boolean, onFirst: () -> Unit, onSecond: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().background(Soft, RoundedCornerShape(6.dp)).padding(3.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        SegmentButton(first, firstSelected, onFirst, Modifier.weight(1f))
        SegmentButton(second, !firstSelected, onSecond, Modifier.weight(1f))
    }
}

@Composable
private fun SegmentButton(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    TextButton(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(5.dp), colors = ButtonDefaults.textButtonColors(containerColor = if (selected) SurfaceColor else Color.Transparent)) {
        Text(label, color = if (selected) Ink else Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ProcessNote() {
    Row(modifier = Modifier.padding(vertical = 14.dp).background(Color(0x08262624), RoundedCornerShape(0.dp))) {
        Box(modifier = Modifier.width(3.dp).fillMaxWidth().background(Accent))
        Column(modifier = Modifier.padding(start = 14.dp, top = 12.dp, end = 14.dp, bottom = 12.dp)) {
            Text("How Communion Is Gathered", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text("Each offering is a complete reference. The Common Witness helps form what is kept; the Hidden Witness keeps quieter Scripture from being forgotten.", color = Muted, fontSize = 13.sp, lineHeight = 22.sp)
        }
    }
}

@Composable
private fun ReferencePill(label: String) {
    Surface(
        color = Paper,
        border = BorderStroke(1.dp, Line),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(label, color = Muted, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
    }
}

@Composable
private fun KeptCommunionContent(communion: KeptCommunion, label: String) {
    Eyebrow(label)
    Spacer(Modifier.height(6.dp))
    Text(communion.gathered.display, color = Ink, fontFamily = FontFamily.Serif, fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
    Text(communion.gathered.preview, color = Ink, fontFamily = FontFamily.Serif, fontSize = 18.sp, lineHeight = 32.sp)
    Spacer(Modifier.height(20.dp))
    Eyebrow("${communion.beneath.size} kept beneath")
    Spacer(Modifier.height(10.dp))
    communion.beneath.forEach { entry ->
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp).border(BorderStroke(1.dp, Line)).padding(14.dp)) {
            Text(entry.display, color = Ink, fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(entry.preview, color = Muted, fontSize = 14.sp, lineHeight = 23.sp)
        }
    }
}

@Composable
private fun ArchiveRow(date: String, reference: String) {
    Row(modifier = Modifier.fillMaxWidth().background(Color(0x08262624), RoundedCornerShape(4.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Eyebrow(date)
            Text(reference, color = Ink, fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
        Text("View", color = Muted, fontSize = 13.sp)
    }
}

@Composable
private fun TextPage(title: String, paragraphs: List<String>) {
    SectionCard {
        Text(title, color = Ink, fontFamily = FontFamily.Serif, fontSize = 32.sp)
        Spacer(Modifier.height(14.dp))
        paragraphs.forEach { paragraph ->
            Text(
                paragraph,
                color = Muted,
                lineHeight = 25.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
private fun Footer(onRouteChange: (PanasheRoute) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 18.dp),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TextButton(onClick = { onRouteChange(PanasheRoute.Communion) }) { Text("Church") }
        TextButton(onClick = { onRouteChange(PanasheRoute.About) }) { Text("About") }
        TextButton(onClick = { onRouteChange(PanasheRoute.Privacy) }) { Text("Privacy Policy") }
    }
}

@Composable
private fun BottomTabs(route: PanasheRoute, onRouteChange: (PanasheRoute) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Paper).border(BorderStroke(0.5.dp, Line)).padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomTab("Daily", route == PanasheRoute.Daily) { onRouteChange(PanasheRoute.Daily) }
        BottomTab("Scripture", route == PanasheRoute.Bible) { onRouteChange(PanasheRoute.Bible) }
        BottomTab("Communion", route == PanasheRoute.Communion) { onRouteChange(PanasheRoute.Communion) }
    }
}

@Composable
private fun BottomTab(label: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick, shape = RoundedCornerShape(0.dp)) {
        Text(label, color = if (selected) Ink else Muted, fontSize = 13.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Line),
        shape = RoundedCornerShape(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(32.dp), content = content)
    }
}

@Composable
private fun Eyebrow(text: String) {
    Text(
        text = text.uppercase(),
        color = Accent,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun PrimaryAction(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White),
        shape = RoundedCornerShape(3.dp)
    ) {
        Text(label)
    }
}

@Composable
private fun SecondaryAction(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        border = BorderStroke(1.dp, Line),
        shape = RoundedCornerShape(3.dp)
    ) {
        Text(label)
    }
}
