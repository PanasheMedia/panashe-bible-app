package org.panashe.bible

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF202734)
private val Muted = Color(0xFF6E7380)
private val Paper = Color(0xFFF7F4EE)
private val Line = Color(0xFFE5DDD2)
private val Gold = Color(0xFFC08A47)

@Composable
fun PanasheApp() {
    var route by remember { mutableStateOf(PanasheRoute.Daily) }

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = Ink,
            secondary = Gold,
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
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier.widthIn(max = 1040.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        when (route) {
                            PanasheRoute.Daily -> DailyReadingScreen(
                                onBible = { route = PanasheRoute.Bible }
                            )
                            PanasheRoute.Bible -> BibleScreen()
                            PanasheRoute.Communion -> CommunionScreen()
                            PanasheRoute.About -> TextPage("About", aboutParagraphs)
                            PanasheRoute.Privacy -> TextPage("Privacy Policy", privacyParagraphs)
                        }
                        Footer(onRouteChange = { route = it })
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(route: PanasheRoute, onRouteChange: (PanasheRoute) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Paper)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "panashe.org",
                    color = Ink,
                    fontFamily = FontFamily.Serif,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = route.path,
                    color = Muted,
                    fontSize = 13.sp
                )
            }
            Text(text = "Bible", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PanasheRoute.entries.forEach { item ->
                NavChip(
                    label = item.title,
                    selected = route == item,
                    onClick = { onRouteChange(item) }
                )
            }
        }
    }
}

@Composable
private fun NavChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = ButtonDefaults.outlinedButtonColors(
        containerColor = if (selected) Color.White else Color.Transparent,
        contentColor = if (selected) Ink else Muted
    )
    OutlinedButton(
        onClick = onClick,
        colors = colors,
        border = BorderStroke(1.dp, if (selected) Color(0xFFD9CEC0) else Line),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(label)
    }
}

@Composable
private fun DailyReadingScreen(onBible: () -> Unit) {
    SectionCard {
        Eyebrow(todayReading.label)
        Spacer(Modifier.height(10.dp))
        Text(
            todayReading.reference,
            color = Ink,
            fontFamily = FontFamily.Serif,
            fontSize = 44.sp,
            lineHeight = 48.sp
        )
        Spacer(Modifier.height(16.dp))
        todayReading.verses.forEach { verse ->
            Text(
                text = verse,
                color = Ink,
                fontFamily = FontFamily.Serif,
                fontSize = 22.sp,
                lineHeight = 34.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }

    SectionCard {
        Eyebrow(todayReading.contextTitle)
        Spacer(Modifier.height(10.dp))
        Text("John 1", color = Ink, fontFamily = FontFamily.Serif, fontSize = 30.sp)
        Spacer(Modifier.height(10.dp))
        Text(todayReading.context, color = Muted, lineHeight = 24.sp)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PrimaryAction("Read the full chapter") {}
            SecondaryAction("Browse Scripture", onBible)
        }
    }
}

@Composable
private fun BibleScreen() {
    SectionCard {
        Eyebrow("Browse Bible")
        Spacer(Modifier.height(10.dp))
        Text("John 1", color = Ink, fontFamily = FontFamily.Serif, fontSize = 34.sp)
        Spacer(Modifier.height(10.dp))
        Text(
            "A clear doorway into the Gospel of John, where the Word, light, and witness begin together.",
            color = Muted,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun CommunionScreen() {
    var hasOffered by remember { mutableStateOf(false) }

    SectionCard {
        Eyebrow("Daily Communion")
        Spacer(Modifier.height(10.dp))
        Text(
            "Today's Reading. Today's Offering. Today's Communion.",
            color = Ink,
            fontFamily = FontFamily.Serif,
            fontSize = 28.sp,
            lineHeight = 36.sp
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Readers offer one complete Scripture reference. Matching references form the Common Witness; quieter offerings remain part of the Hidden Witness.",
            color = Muted,
            lineHeight = 24.sp
        )
    }

    SectionCard {
        Eyebrow("Today's Reading")
        Spacer(Modifier.height(8.dp))
        Text(todayReading.reference, color = Ink, fontFamily = FontFamily.Serif, fontSize = 28.sp)
        Spacer(Modifier.height(8.dp))
        Text("Begin with today's 3 verses, then offer one reference in response.", color = Muted, lineHeight = 24.sp)
        Spacer(Modifier.height(14.dp))
        PrimaryAction("Read today's 3 verses") {}
    }

    SectionCard {
        Eyebrow("Today's Offering")
        Spacer(Modifier.height(8.dp))
        Text("Offer one reference", color = Ink, fontFamily = FontFamily.Serif, fontSize = 28.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            "Choose book, chapter, and 1-3 consecutive verses. If another reader offers the same verse, it gains a private count.",
            color = Muted,
            lineHeight = 24.sp
        )
        Spacer(Modifier.height(14.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ReferencePill("Book")
            ReferencePill("Chapter")
            ReferencePill("Start")
            ReferencePill("End")
        }
        Spacer(Modifier.height(14.dp))
        if (hasOffered) {
            Text(
                "Your offering has been received for today. It stays private unless gathered.",
                color = Gold,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(10.dp))
            PrimaryAction("Offering Received") {}
        } else {
            PrimaryAction("Submit today's offering") { hasOffered = true }
        }
    }

    SectionCard {
        Eyebrow("Today's Communion")
        Spacer(Modifier.height(8.dp))
        Text("View the kept seven", color = Ink, fontFamily = FontFamily.Serif, fontSize = 28.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            "The public page shows one gathered passage with six beneath it. Names, counts, rankings, and non-kept offerings stay hidden.",
            color = Muted,
            lineHeight = 24.sp
        )
    }

    KeptCommunionCard(todayCommunion)
}

@Composable
private fun ReferencePill(label: String) {
    Surface(
        color = Color(0xFFFBFAF7),
        border = BorderStroke(1.dp, Line),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(label, color = Muted, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
    }
}

@Composable
private fun KeptCommunionCard(communion: KeptCommunion) {
    SectionCard {
        Eyebrow(communion.date)
        Spacer(Modifier.height(10.dp))
        Text(communion.gathered.reference, color = Ink, fontFamily = FontFamily.Serif, fontSize = 30.sp)
        Spacer(Modifier.height(8.dp))
        Text(communion.gathered.preview, color = Ink, lineHeight = 25.sp)
        Spacer(Modifier.height(18.dp))
        Eyebrow("${communion.beneath.size} kept beneath")
        Spacer(Modifier.height(10.dp))
        communion.beneath.forEach { entry ->
            Text(entry.reference, color = Ink, fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(entry.preview, color = Muted, lineHeight = 23.sp, modifier = Modifier.padding(bottom = 12.dp))
        }
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
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Line),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(22.dp), content = content)
    }
}

@Composable
private fun Eyebrow(text: String) {
    Text(
        text = text.uppercase(),
        color = Gold,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun PrimaryAction(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(label)
    }
}

@Composable
private fun SecondaryAction(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        border = BorderStroke(1.dp, Color(0xFFC9B8A5)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(label)
    }
}
