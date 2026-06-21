package org.panashe.bible.features.communion

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.ui.draw.alpha
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommunionScreen(view: CommunionView?) {
    var hasOffered by remember { mutableStateOf(false) }
    var selectedTopTab by remember { mutableStateOf("read") }
    var selectedKeptTab by remember { mutableStateOf("today") }

    val reading = view?.reading
    val kept = view?.kept

    CommunionHero()

    // Top card with accent top border (matches web .communion-top-card)
    TopCard {
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
            Spacer(Modifier.height(6.dp))
            Text(
                reading?.display ?: "Loading...",
                color = Ink,
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 28.sp
            )
            Spacer(Modifier.height(14.dp))
            Text(
                reading?.chapterIntro ?: "Loading the chapter context from bundled Scripture.",
                color = Muted,
                fontSize = 14.sp,
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(14.dp))
            PrimaryAction("Read the full chapter") {}
        } else {
            Text(
                "Offer One Reference",
                color = Ink,
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
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
            Text(
                "After you offer, this form closes for today. The kept Communion is assembled privately from all offerings.",
                color = Muted,
                fontSize = 13.sp,
                lineHeight = 21.sp
            )
            Spacer(Modifier.height(14.dp))
            if (hasOffered) {
                Text(
                    "Your offering has been received for today.",
                    color = Accent,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 22.sp
                )
            } else {
                PrimaryAction("Submit today's offering") { hasOffered = true }
            }
        }
    }

    // Kept Seven card with Reddit-style thread layout
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
            RedditThread(kept, if (selectedKeptTab == "today") "Today" else "Previous")
        }
    }

    // Archive card with grid layout
    SectionCard {
        Text(
            "Archive",
            color = Ink,
            fontFamily = FontFamily.Serif,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Previous gathered Communions, preserved. Tap any entry to see the final Scripture shown without counts, names, or rankings.",
            color = Muted,
            fontSize = 14.sp,
            lineHeight = 24.sp
        )
        Spacer(Modifier.height(14.dp))
        if (kept != null) {
            ArchiveGrid(kept)
        }
    }
}

@Composable
fun CommunionHero() {
    val infiniteTransition = rememberInfiniteTransition(label = "cross")
    val crossAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "crossAlpha"
    )

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 54.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "\u2726",
            color = Accent,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.alpha(crossAlpha)
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Daily Communion",
            color = Ink,
            fontFamily = FontFamily.Serif,
            fontSize = 52.sp,
            lineHeight = 54.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
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
        FlowRow(
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CommunionStep("1", "Read today's 3 verses")
            CommunionStep("2", "Offer one reference")
            CommunionStep("3", "View the kept seven")
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "SCRIPTURE ONLY. NO PROFILES, COMMENTS, SCORES, OR RANKINGS.",
            color = Accent,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CommunionStep(number: String, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier.size(22.dp)
                .background(Color(0x1FC64F35), RoundedCornerShape(50)),
            contentAlignment = Alignment.Center
        ) {
            Text(number, color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Text(label, color = Muted, fontSize = 13.sp)
    }
}

@Composable
fun SegmentedTabs(first: String, second: String, firstSelected: Boolean, onFirst: () -> Unit, onSecond: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(Soft, RoundedCornerShape(8.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SegmentButton(first, firstSelected, onFirst, Modifier.weight(1f))
        SegmentButton(second, !firstSelected, onSecond, Modifier.weight(1f))
    }
}

@Composable
fun SegmentButton(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (selected) SurfaceColor else Color.Transparent
        )
    ) {
        Text(
            label,
            color = if (selected) Ink else Muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ProcessNote() {
    Row(
        modifier = Modifier.padding(vertical = 14.dp)
            .background(
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
                RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
            )
    ) {
        Box(
            modifier = Modifier.width(3.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.secondary)
        )
        Column(modifier = Modifier.padding(start = 14.dp, top = 14.dp, end = 16.dp, bottom = 14.dp)) {
            Text(
                "How Communion Is Gathered",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Each offering is a complete reference. The Common Witness helps form what is kept; the Hidden Witness keeps quieter Scripture from being forgotten.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 13.sp,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun DropdownSelector(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
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

// --- Reddit-style thread layout (matches web .reddit-thread) ---

@Composable
fun RedditThread(communion: KeptCommunion, label: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Gathered passage (OP post with accent left border)
        RedditPost(communion)

        // Comments header
        Text(
            "${communion.beneath.size} KEPT BENEATH",
            color = Muted,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 18.dp, top = 12.dp, bottom = 8.dp)
        )

        // Individual offerings (comments with thread lines)
        Column(modifier = Modifier.fillMaxWidth()) {
            communion.beneath.forEach { entry ->
                RedditComment(entry)
            }
        }
    }
}

@Composable
fun RedditPost(communion: KeptCommunion) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Left accent border (matches web .reddit-post border-left: 3px solid var(--accent))
        Box(
            modifier = Modifier.width(3.dp)
                .background(Accent)
        )
        Column(
            modifier = Modifier
                .background(Color(0x05000000)) // ink 2% tint
                .padding(22.dp)
                .fillMaxWidth()
        ) {
            // Meta: OP label + date
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "GATHERED PASSAGE",
                    color = Accent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.6.sp
                )
                Text(
                    communion.date,
                    color = Muted,
                    fontSize = 10.sp
                )
            }
            Spacer(Modifier.height(14.dp))
            // Scripture text
            Text(
                communion.gathered.display,
                color = Ink,
                fontFamily = FontFamily.Serif,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 28.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                communion.gathered.preview,
                color = Ink,
                fontFamily = FontFamily.Serif,
                fontSize = 15.sp,
                lineHeight = 26.sp
            )
        }
    }
}

@Composable
fun RedditComment(entry: CommunionEntry) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(start = 14.dp)
            .hoverable(interactionSource)
    ) {
        // Thread line (vertical)
        Box(
            modifier = Modifier.width(2.dp)
                .background(if (isHovered) Accent else Line)
        )
        // Horizontal connector + content
        Column(modifier = Modifier.fillMaxWidth().padding(start = 18.dp)) {
            // Horizontal connector line
            Box(
                modifier = Modifier.width(14.dp).height(2.dp)
                    .background(if (isHovered) Accent else Line)
            )
            // Content
            Column(modifier = Modifier.padding(top = 12.dp, bottom = 16.dp)) {
                // Reference + Read link
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        entry.display,
                        color = Ink,
                        fontFamily = FontFamily.Serif,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Read chapter",
                        color = Accent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(8.dp))
                // Expandable verse text
                Text(
                    entry.preview,
                    color = Muted,
                    fontSize = 13.sp,
                    lineHeight = 22.sp
                )
            }
            // Bottom border (60% line opacity)
            Box(
                modifier = Modifier.fillMaxWidth().height(1.dp)
                    .background(Line.copy(alpha = 0.6f))
            )
        }
    }
}

// --- Top card with accent top border ---

@Composable
fun TopCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Line),
        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .border(2.dp, Accent, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .padding(horizontal = 24.dp, vertical = 26.dp),
            content = content
        )
    }
}

// --- Archive grid (matches web .communion-archive-list) ---

@Composable
fun ArchiveGrid(communion: KeptCommunion) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ArchiveItem(
            date = communion.date,
            reference = communion.gathered.display,
            count = communion.beneath.size,
            isToday = true,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ArchiveItem(date: String, reference: String, count: Int, isToday: Boolean, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Surface(
        color = if (isHovered) Color(0x08000000) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (isToday || isHovered) Accent else Line
        ),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier.hoverable(interactionSource)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(date, color = Muted, fontSize = 10.sp, lineHeight = 14.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                reference,
                color = Ink,
                fontFamily = FontFamily.Serif,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "$count kept beneath",
                color = Muted,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}
