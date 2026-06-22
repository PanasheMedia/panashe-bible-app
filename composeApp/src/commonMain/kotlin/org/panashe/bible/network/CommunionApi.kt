package org.panashe.bible.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
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

    companion object {
        const val DEFAULT_BASE_URL = "https://panashe-bible-api.garande.workers.dev/api/v1"

        fun defaultClient(): HttpClient = HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }
        }
    }
}
