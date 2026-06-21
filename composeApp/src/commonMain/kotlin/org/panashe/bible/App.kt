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
import org.panashe.bible.features.communion.CommunionRepository
import org.panashe.bible.features.communion.CommunionScreen
import org.panashe.bible.features.communion.CommunionView
import org.panashe.bible.features.communion.StaticCommunionRepository
import org.panashe.bible.features.pages.TextPage
import org.panashe.bible.features.reader.BibleScreen
import org.panashe.bible.features.reader.DailyReadingScreen
import org.panashe.bible.features.reader.MutableReaderPreferences
import org.panashe.bible.features.reader.SearchDialog
import org.panashe.bible.features.reader.SettingsDialog
import org.panashe.bible.platform.AppSettings
import org.panashe.bible.platform.PersistedReaderPrefs
import org.panashe.bible.shared.SharedConstants
import org.panashe.bible.ui.Ink
import org.panashe.bible.ui.Line
import org.panashe.bible.ui.Muted
import org.panashe.bible.ui.PanasheTheme
import org.panashe.bible.ui.Paper

@Composable
fun PanasheApp(
    repository: CommunionRepository = StaticCommunionRepository(),
    appSettings: AppSettings? = null
) {
    var route by remember { mutableStateOf(PanasheRoute.Daily) }
    var view by remember { mutableStateOf<CommunionView?>(null) }
    var bibleData by remember { mutableStateOf<BibleData?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    // Bible tab navigation state
    var bibleBookSlug by remember { mutableStateOf<String?>(null) }
    var bibleChapter by remember { mutableStateOf(1) }
    var showSearch by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    // Load persisted state
    val persistedState = remember { appSettings?.load() }
    val readerPrefs = remember {
        val prefs = persistedState?.readerPrefs
        MutableReaderPreferences().apply {
            if (prefs != null) {
                fontFamily = when (prefs.fontFamily) {
                    "sansSerif" -> FontFamily.SansSerif
                    "monospace" -> FontFamily.Monospace
                    else -> FontFamily.Serif
                }
                fontLabel = prefs.fontLabel
                textSizePercent = prefs.textSizePercent
                showVerseNumbers = prefs.showVerseNumbers
                lineByLine = prefs.lineByLine
            }
        }
    }
    // Restore last position
    val initialBook = remember { persistedState?.lastBookSlug ?: "john" }
    val initialChapter = remember { persistedState?.lastChapter ?: 1 }

    LaunchedEffect(repository) {
        runCatching {
            val v = repository.todayView()
            val d = repository.bibleData()
            view = v
            bibleData = d
            // Use persisted position if available, otherwise default to reading's book/chapter
            if (bibleBookSlug == null) {
                bibleBookSlug = initialBook
                bibleChapter = initialChapter
            }
        }.onFailure { loadError = it.message ?: "Unable to load bundled Bible data." }
    }

    // Persist position and prefs on changes
    LaunchedEffect(bibleBookSlug, bibleChapter) {
        if (bibleBookSlug != null && appSettings != null) {
            appSettings.update {
                copy(lastBookSlug = bibleBookSlug ?: "john", lastChapter = bibleChapter)
            }
        }
    }

    PanasheTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Paper) {
            Column(modifier = Modifier.fillMaxSize()) {
                Header(route = route, onRouteChange = { route = it }, onSearch = { showSearch = true }, onSettings = { showSettings = true })
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
                            PanasheRoute.Bible -> BibleScreen(
                                view = view,
                                loadError = loadError,
                                bibleData = bibleData,
                                bookSlug = bibleBookSlug ?: view?.reading?.reference?.book ?: "john",
                                chapter = bibleChapter,
                                onBookChange = { slug -> bibleBookSlug = slug; bibleChapter = 1 },
                                onChapterChange = { ch -> bibleChapter = ch },
                                prefs = readerPrefs
                            )
                            PanasheRoute.Communion -> CommunionScreen(
                                view = view,
                                bibleData = bibleData,
                                appSettings = appSettings
                            )
                            PanasheRoute.About -> TextPage("About", aboutParagraphs)
                            PanasheRoute.Privacy -> TextPage("Privacy Policy", privacyParagraphs)
                        }
                        Footer(onRouteChange = { route = it })
                    }
                }
                BottomTabs(route = route, onRouteChange = { route = it })
            }

            if (showSearch && bibleData != null) {
                SearchDialog(
                    bibleData = bibleData!!,
                    onDismissRequest = { showSearch = false },
                    onNavigateToVerse = { slug, ch, _ ->
                        bibleBookSlug = slug
                        bibleChapter = ch
                        route = PanasheRoute.Bible
                    }
                )
            }

            if (showSettings) {
                SettingsDialog(
                    prefs = readerPrefs,
                    onDismissRequest = {
                        showSettings = false
                        // Persist reader preferences on close
                        if (appSettings != null) {
                            val snapshot = readerPrefs.snapshot()
                            appSettings.update {
                                copy(readerPrefs = PersistedReaderPrefs(
                                    fontFamily = when (snapshot.fontFamily) {
                                        FontFamily.SansSerif -> "sansSerif"
                                        FontFamily.Monospace -> "monospace"
                                        else -> "serif"
                                    },
                                    fontLabel = snapshot.fontLabel,
                                    textSizePercent = snapshot.textSizePercent,
                                    showVerseNumbers = snapshot.showVerseNumbers,
                                    lineByLine = snapshot.lineByLine
                                ))
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun Header(route: PanasheRoute, onRouteChange: (PanasheRoute) -> Unit, onSearch: () -> Unit = {}, onSettings: () -> Unit = {}) {
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
                    HeaderAction("Aa", onClick = onSettings)
                    HeaderAction("Search", onClick = onSearch)
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
private fun HeaderAction(label: String, onClick: () -> Unit = {}) {
    TextButton(onClick = onClick, shape = RoundedCornerShape(50)) {
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
