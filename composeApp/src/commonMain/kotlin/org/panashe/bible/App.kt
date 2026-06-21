package org.panashe.bible

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.panashe.bible.features.communion.CommunionScreen
import org.panashe.bible.features.communion.CommunionView
import org.panashe.bible.features.pages.TextPage
import org.panashe.bible.features.reader.BibleScreen
import org.panashe.bible.features.reader.DailyReadingScreen
import org.panashe.bible.ui.Ink
import org.panashe.bible.ui.Line
import org.panashe.bible.ui.Muted
import org.panashe.bible.ui.PanasheTheme
import org.panashe.bible.ui.Paper

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

    PanasheTheme {
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
