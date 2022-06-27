package dev.schlaubi.hafalsch.marudor

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json


public inline fun Marudor(builder: MarudorBuilder.() -> Unit = {}): Marudor = MarudorBuilder().apply(builder).build()

public class MarudorBuilder @PublishedApi internal constructor() {
    public var engine: HttpClientEngineFactory<*>? = null
    public var json: Json? = null
    public var httpClient: HttpClient? = null
    public var url: Url = DEFAULT_MARUDOR_URL

    @PublishedApi
    internal fun build(): Marudor {
        require(engine == null || httpClient == null) { "Please specify either an engine or an httpClient" }
        val client = (httpClient ?: engine?.let { HttpClient(it) } ?: HttpClient()).config {
            install(ContentNegotiation) {
                val json = this@MarudorBuilder.json ?: Json {
                    ignoreUnknownKeys = true
                }
                json(json)

            }
            install(Resources)

            defaultRequest {
                url {
                    takeFrom(this@MarudorBuilder.url)
                    appendPathSegments(this@MarudorBuilder.url.pathSegments.filterNot(String::isBlank))
                }
            }
        }

        val resources = MarudorResources(client, url)

        return Marudor(resources)
    }
}
