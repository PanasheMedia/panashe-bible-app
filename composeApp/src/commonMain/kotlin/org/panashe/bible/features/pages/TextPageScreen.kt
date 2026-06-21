package org.panashe.bible.features.pages

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.panashe.bible.ui.Ink
import org.panashe.bible.ui.Muted
import org.panashe.bible.ui.components.SectionCard

@Composable
fun TextPage(title: String, paragraphs: List<String>) {
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
