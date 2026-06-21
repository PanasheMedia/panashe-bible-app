package org.panashe.bible.features.communion

import org.panashe.bible.shared.ScriptureReference

/**
 * Interface defining local storage operations for Communion.
 * Platform-specific implementations (e.g. SQLDelight, Room) can fulfill this interface
 * without leaking database-specific dependencies into the UI layer.
 */
interface CommunionStorage {
    suspend fun saveOffering(reference: ScriptureReference)
    suspend fun hasOfferedToday(dateIso: String): Boolean
    suspend fun getOfferings(): List<ScriptureReference>
}

class InMemoryCommunionStorage : CommunionStorage {
    private val offerings = mutableListOf<ScriptureReference>()
    private var lastOfferingDate: String? = null

    override suspend fun saveOffering(reference: ScriptureReference) {
        offerings.add(reference)
    }

    override suspend fun hasOfferedToday(dateIso: String): Boolean {
        return lastOfferingDate == dateIso
    }

    override suspend fun getOfferings(): List<ScriptureReference> {
        return offerings.toList()
    }
}
