package org.panashe.bible.platform

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PersistedReaderPrefs(
    val fontFamily: String = "serif",
    val fontLabel: String = "Source Serif",
    val textSizePercent: Float = 100f,
    val showVerseNumbers: Boolean = true,
    val lineByLine: Boolean = false
)

@Serializable
data class PersistedAppState(
    val readerPrefs: PersistedReaderPrefs = PersistedReaderPrefs(),
    val lastBookSlug: String = "john",
    val lastChapter: Int = 1,
    val offeredTodayIso: String? = null,
    val clientId: String? = null
)

class AppSettings(private val filePath: String) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }
    private var cached: PersistedAppState? = null

    fun load(): PersistedAppState {
        cached?.let { return it }
        return try {
            val raw = readFile(filePath)
            if (raw.isNotBlank()) {
                val data = json.decodeFromString<PersistedAppState>(raw)
                cached = data
                data
            } else {
                PersistedAppState().also { cached = it }
            }
        } catch (_: Exception) {
            PersistedAppState().also { cached = it }
        }
    }

    fun save(state: PersistedAppState) {
        cached = state
        writeFile(filePath, json.encodeToString(PersistedAppState.serializer(), state))
    }

    fun update(transform: PersistedAppState.() -> PersistedAppState) {
        save(load().transform())
    }
}
