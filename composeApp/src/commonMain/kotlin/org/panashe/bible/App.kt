package org.panashe.bible

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.panashe.bible.features.communion.CommunionRepository
import org.panashe.bible.features.communion.CommunionScreen
import org.panashe.bible.features.communion.CommunionView
import org.panashe.bible.features.communion.RemoteCommunionRepository
import org.panashe.bible.features.communion.StaticCommunionRepository
import org.panashe.bible.features.pages.SupportSection
import org.panashe.bible.features.pages.TextPage
import org.panashe.bible.features.reader.BibleScreen
import org.panashe.bible.features.reader.DailyReadingScreen
import org.panashe.bible.features.reader.MutableReaderPreferences
import org.panashe.bible.features.reader.SearchDialog
import org.panashe.bible.features.reader.SettingsDialog
import org.panashe.bible.platform.AppSettings
import org.panashe.bible.platform.PersistedReaderPrefs
import org.panashe.bible.shared.SharedConstants
import org.panashe.bible.ui.PanasheTheme
import org.panashe.bible.ui.components.MoonIcon
import org.panashe.bible.ui.components.SearchIcon
import org.panashe.bible.ui.components.SunIcon

/** Anonymous, stable-per-install client id for the backend's once-per-day rule. */
private fun randomClientId(): String =
    buildString { repeat(32) { append("0123456789abcdef".random()) } }

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
    // Theme: null = follow system, "light"/"dark" = explicit user choice.
    var darkModePref by remember { mutableStateOf(persistedState?.darkMode) }
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (darkModePref) {
        "dark" -> true
        "light" -> false
        else -> systemDark
    }
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
                audioSpeed = prefs.audioSpeed
                audioVoiceIndex = prefs.audioVoiceIndex
            }
        }
    }
    // Restore last position
    val initialBook = remember { persistedState?.lastBookSlug ?: "john" }
    val initialChapter = remember { persistedState?.lastChapter ?: 1 }

    // Stable anonymous client id for the backend's once-per-day offering rule.
    val clientId = remember {
        persistedState?.clientId?.takeIf { it.isNotBlank() }
            ?: randomClientId().also { id -> appSettings?.update { copy(clientId = id) } }
    }
    // Daily Communion reads come from the shared backend when available.
    val remoteRepo = remember(clientId, repository) {
        RemoteCommunionRepository(clientId = clientId, delegate = repository)
    }

    LaunchedEffect(remoteRepo) {
        runCatching {
            val v = remoteRepo.todayView()
            val d = remoteRepo.bibleData()
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

    PanasheTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val wide = maxWidth > 700.dp
            val scrollState = rememberScrollState()
            // Auto-hide chrome: collapse header + bottom tabs while scrolling down, reveal on scroll up.
            var chromeVisible by remember { mutableStateOf(true) }
            var lastScroll by remember { mutableStateOf(0) }
            val readingRoute = route == PanasheRoute.Bible
            LaunchedEffect(scrollState) {
                snapshotFlow { scrollState.value }.collect { value ->
                    chromeVisible = when {
                        value <= 0 -> true
                        value - lastScroll > 6 -> false
                        lastScroll - value > 6 -> true
                        else -> chromeVisible
                    }
                    lastScroll = value
                }
            }
            Column(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = chromeVisible,
                    enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                ) {
                    Header(
                        route = route,
                        onRouteChange = { route = it },
                        wide = wide,
                        showRouteTabs = !readingRoute,
                        isDark = darkTheme,
                        onToggleTheme = {
                            val next = if (darkTheme) "light" else "dark"
                            darkModePref = next
                            appSettings?.update { copy(darkMode = next) }
                        },
                        onSearch = { showSearch = true },
                        onSettings = { showSettings = true }
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
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
                                onBible = { slug, chapter ->
                                    if (slug != null && chapter != null) {
                                        bibleBookSlug = slug
                                        bibleChapter = chapter
                                    }
                                    route = PanasheRoute.Bible
                                }
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
                                onReadChapter = { slug, ch ->
                                    bibleBookSlug = slug
                                    bibleChapter = ch
                                    route = PanasheRoute.Bible
                                }
                            )
                            PanasheRoute.About -> {
                                TextPage("About", aboutParagraphs)
                                SupportSection()
                            }
                            PanasheRoute.Privacy -> TextPage("Privacy Policy", privacyParagraphs)
                        }
                        Footer(onRouteChange = { route = it })
                    }
                }
                if (!wide) AnimatedVisibility(
                    visible = chromeVisible && !readingRoute,
                    enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                    exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
                ) {
                    BottomTabs(route = route, onRouteChange = { route = it })
                }
            }
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
                                    lineByLine = snapshot.lineByLine,
                                    audioSpeed = snapshot.audioSpeed,
                                    audioVoiceIndex = snapshot.audioVoiceIndex
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
private fun Header(
    route: PanasheRoute,
    onRouteChange: (PanasheRoute) -> Unit,
    wide: Boolean,
    showRouteTabs: Boolean,
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    onSearch: () -> Unit = {},
    onSettings: () -> Unit = {}
) {
    // Web .site-header: sticky bar with a single 1px bottom divider (not a 4-sided box).
    Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = if (wide) 40.dp else 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "The Bible",
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.weight(1f))
            if (wide && showRouteTabs) {
                Row(horizontalArrangement = Arrangement.spacedBy(32.dp), verticalAlignment = Alignment.CenterVertically) {
                    PrimaryTab("Daily", route == PanasheRoute.Daily) { onRouteChange(PanasheRoute.Daily) }
                    PrimaryTab("Scripture", route == PanasheRoute.Bible) { onRouteChange(PanasheRoute.Bible) }
                    PrimaryTab("Communion", route == PanasheRoute.Communion) { onRouteChange(PanasheRoute.Communion) }
                }
                Spacer(Modifier.weight(1f))
            }
            // Action icons — always reachable, at every width.
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onSettings) {
                    Text("Aa", color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
                IconButton(onClick = onToggleTheme) {
                    Icon(
                        imageVector = if (isDark) SunIcon else MoonIcon,
                        contentDescription = if (isDark) "Switch to light mode" else "Switch to dark mode",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onSearch) {
                    Icon(
                        imageVector = SearchIcon,
                        contentDescription = "Search the Bible",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline))
    }
}

@Composable
private fun PrimaryTab(label: String, selected: Boolean, onClick: () -> Unit) {
    // Web .primary-tabs a: muted → ink when active, with a 2px ink underline.
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(vertical = 6.dp)
    ) {
        Text(
            label,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(if (selected) MaterialTheme.colorScheme.onSurface else androidx.compose.ui.graphics.Color.Transparent)
        )
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
    // Web .mobile-tabs: single 1px top divider; active tab gets a soft background.
    Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline))
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTab("Daily", route == PanasheRoute.Daily) { onRouteChange(PanasheRoute.Daily) }
            BottomTab("Scripture", route == PanasheRoute.Bible) { onRouteChange(PanasheRoute.Bible) }
            BottomTab("Communion", route == PanasheRoute.Communion) { onRouteChange(PanasheRoute.Communion) }
        }
    }
}

@Composable
private fun BottomTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .background(if (selected) MaterialTheme.colorScheme.surfaceVariant else androidx.compose.ui.graphics.Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}
