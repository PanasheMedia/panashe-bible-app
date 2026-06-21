package org.panashe.bible.features.reader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.panashe.bible.features.communion.CommunionView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import org.panashe.bible.ui.Ink
import org.panashe.bible.ui.Line
import org.panashe.bible.ui.Muted
import org.panashe.bible.ui.components.Eyebrow
import org.panashe.bible.ui.components.Hero
import org.panashe.bible.ui.components.LoadingText
import org.panashe.bible.ui.components.PrimaryAction
import org.panashe.bible.ui.components.SecondaryAction
import org.panashe.bible.ui.components.SectionCard

@Composable
fun DailyReadingScreen(view: CommunionView?, loadError: String?, onBible: () -> Unit) {
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
fun BibleScreen(view: CommunionView?, loadError: String?) {
    val reading = view?.reading
    ReaderToolbar(reading)
    SectionCard {
        Eyebrow(reading?.chapterTitle ?: "Scripture")
        Spacer(Modifier.height(10.dp))
        Text(reading?.chapterTitle ?: "Bible", color = Ink, fontFamily = FontFamily.Serif, fontSize = 52.sp, lineHeight = 56.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        Text(
            reading?.chapterIntro ?: "Loading the bundled Scripture text.",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = 13.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            IconButton(onClick = { /* Audio playback logic */ }) {
                Text("▶", color = MaterialTheme.colorScheme.onSurface, fontSize = 24.sp)
            }
        }
        Spacer(Modifier.height(26.dp))
        when {
            loadError != null -> LoadingText(loadError)
            reading == null -> LoadingText("Loading Scripture...")
            else -> {
                reading.chapterVerses.forEach { verse ->
                    Text(
                        text = "${verse.number}  ${verse.text}",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Serif,
                        fontSize = 18.sp,
                        lineHeight = 33.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                ChapterNav()
            }
        }
    }
}

@Composable
fun ChapterNav() {
    Spacer(Modifier.height(40.dp))
    Row(
        modifier = Modifier.fillMaxWidth().border(BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)), RoundedCornerShape(4.dp)).padding(18.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).clickable {  }) {
            Eyebrow("Previous")
            Spacer(Modifier.height(4.dp))
            Text("Chapter 1", fontFamily = FontFamily.Serif, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Column(modifier = Modifier.weight(1f).clickable {  }, horizontalAlignment = Alignment.End) {
            Eyebrow("Next")
            Spacer(Modifier.height(4.dp))
            Text("Chapter 3", fontFamily = FontFamily.Serif, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ReaderToolbar(reading: DailyReading?) {
    val bookName = reading?.chapterTitle?.substringBeforeLast(" ") ?: "Book"
    val chapterNumber = reading?.reference?.chapter?.toString() ?: "1"
    Row(modifier = Modifier.fillMaxWidth().border(BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))), verticalAlignment = Alignment.CenterVertically) {
        ToolbarSelector("Book", bookName, Modifier.weight(1f))
        ToolbarSelector("Chapter", chapterNumber, Modifier.weight(1f))
        ToolbarSelector("Translation", "KJVA", Modifier.weight(1f))
    }
}

@Composable
fun ToolbarSelector(label: String, value: String, modifier: Modifier) {
    Column(modifier = modifier.clickable {  }.padding(14.dp)) {
        Eyebrow(label)
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontFamily = FontFamily.Serif, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}
