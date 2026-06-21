package org.panashe.bible.features.communion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.panashe.bible.ui.Accent
import org.panashe.bible.ui.Ink
import org.panashe.bible.ui.Line
import org.panashe.bible.ui.Muted
import org.panashe.bible.ui.Paper
import org.panashe.bible.ui.Soft
import org.panashe.bible.ui.SurfaceColor
import org.panashe.bible.ui.components.Eyebrow
import org.panashe.bible.ui.components.LoadingText
import org.panashe.bible.ui.components.PrimaryAction
import org.panashe.bible.ui.components.SectionCard

@Composable
fun CommunionScreen(view: CommunionView?) {
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
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                DropdownSelector("Book", "Select book")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DropdownSelector("Chapter", "1", Modifier.weight(1f))
                    DropdownSelector("Start", "1", Modifier.weight(1f))
                    DropdownSelector("End", "1", Modifier.weight(1f))
                }
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
fun CommunionHero() {
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
fun CommunionStep(number: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 8.dp)) {
        Box(modifier = Modifier.size(22.dp).background(Color(0x1FC64F35), RoundedCornerShape(50)), contentAlignment = Alignment.Center) {
            Text(number, color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Text(label, color = Muted, fontSize = 13.sp)
    }
}

@Composable
fun SegmentedTabs(first: String, second: String, firstSelected: Boolean, onFirst: () -> Unit, onSecond: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().background(Soft, RoundedCornerShape(6.dp)).padding(3.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        SegmentButton(first, firstSelected, onFirst, Modifier.weight(1f))
        SegmentButton(second, !firstSelected, onSecond, Modifier.weight(1f))
    }
}

@Composable
fun SegmentButton(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    TextButton(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(5.dp), colors = ButtonDefaults.textButtonColors(containerColor = if (selected) SurfaceColor else Color.Transparent)) {
        Text(label, color = if (selected) Ink else Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
    }
}

@Composable
fun ProcessNote() {
    Row(modifier = Modifier.padding(vertical = 14.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f), RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))) {
        Box(modifier = Modifier.width(3.dp).fillMaxHeight().background(MaterialTheme.colorScheme.secondary))
        Column(modifier = Modifier.padding(start = 14.dp, top = 14.dp, end = 16.dp, bottom = 14.dp)) {
            Text("How Communion Is Gathered", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text("Each offering is a complete reference. The Common Witness helps form what is kept; the Hidden Witness keeps quieter Scripture from being forgotten.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 13.sp, lineHeight = 22.sp)
        }
    }
}

@Composable
fun DropdownSelector(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Surface(
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth().height(44.dp)
        ) {
            Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(horizontal = 14.dp)) {
                Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun KeptCommunionContent(communion: KeptCommunion, label: String) {
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
fun ArchiveRow(date: String, reference: String) {
    Row(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f), RoundedCornerShape(4.dp)).border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(4.dp)).padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Eyebrow(date)
            Spacer(Modifier.height(6.dp))
            Text(reference, color = MaterialTheme.colorScheme.onSurface, fontFamily = FontFamily.Serif, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        Text("View", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 13.sp)
    }
}
