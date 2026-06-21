package org.panashe.bible.features.reader

/**
 * Interface defining local storage operations for Bible reading preferences.
 */
interface BiblePreferencesStorage {
    suspend fun savePreferredTranslation(translationSlug: String)
    suspend fun getPreferredTranslation(): String?
}

class InMemoryBiblePreferencesStorage : BiblePreferencesStorage {
    private var preferredTranslation: String? = null

    override suspend fun savePreferredTranslation(translationSlug: String) {
        preferredTranslation = translationSlug
    }

    override suspend fun getPreferredTranslation(): String? {
        return preferredTranslation
    }
}
