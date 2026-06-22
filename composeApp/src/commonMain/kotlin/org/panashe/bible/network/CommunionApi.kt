package org.panashe.bible.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Reader offering sent to the shared backend (Cloudflare Worker + D1). */
@Serializable
data class OfferRequest(
    val date: String,
    val book: String,
    val chapter: Int,
    val startVerse: Int,
    val endVerse: Int,
    val clientId: String,
    val translation: String = "kjva",
)

@Serializable
data class OfferResponse(
    val accepted: Boolean = false,
    val alreadyOffered: Boolean = false,
)

/** A Scripture reference as returned by the API (display name + slug, no counts). */
@Serializable
data class WireRef(
    val book: String,
    val slug: String,
    val chapter: Int,
    val start: Int,
    val end: Int,
)

/** The day's reading + witness gathered live by the server (counts are never sent). */
@Serializable
data class CommunionResponse(
    val date: String,
    val dateLabel: String = "",
    val reading: WireRef,
    val communion: List<WireRef> = emptyList(),
    val commonWitness: List<WireRef> = emptyList(),
    val hiddenWitness: List<WireRef> = emptyList(),
)

/**
 * Thin client for the Panashe Bible API. The Ktor engine is selected
 * automatically per platform (Darwin on iOS, OkHttp on Android).
 */
class CommunionApi(
    private val baseUrl: String = DEFAULT_BASE_URL,
    private val client: HttpClient = defaultClient(),
) {
    suspend fun submitOffering(request: OfferRequest): OfferResponse =
        client.post("$baseUrl/communion/offer") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    /** Today's reading + the witness gathered live by the server. */
    suspend fun getCommunion(date: String): CommunionResponse =
        client.get("$baseUrl/communion") {
            parameter("date", date)
        }.body()

    companion object {
        const val DEFAULT_BASE_URL = "https://panashe-bible-api.garande.workers.dev/api/v1"

        fun defaultClient(): HttpClient = HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }
        }
    }
}
