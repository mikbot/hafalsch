package dev.schlaubi.hafalsch.client

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json


public inline operator fun <E, Builder : ClientBuilder<E>, T : ClientCompanion<E, Builder>>
        T.invoke(builder: Builder.() -> Unit = {}): E = newBuilder().apply(builder).build()

public interface ClientCompanion<E, T : ClientBuilder<E>> {
    public fun newBuilder(): T
}

public abstract class ClientBuilder<T> protected constructor() {
    public abstract val defaultUrl: Url
    public var engine: HttpClientEngineFactory<*>? = null
    public var json: Json? = null
    public var httpClient: HttpClient? = null
    public var url: Url? = null

    public inline fun url(url: Url, builder: URLBuilder.() -> Unit = {}) {
        this.url = URLBuilder(url).apply(builder).build()
    }

    public inline fun url(url: String, builder: URLBuilder.() -> Unit = {}): Unit = url(Url(url), builder)

    @PublishedApi
    internal fun build(): T {
        require(engine == null || httpClient == null) { "Please specify either an engine or an httpClient" }
        val safeUrl = this@ClientBuilder.url ?: defaultUrl

        val client = (httpClient ?: engine?.let(::HttpClient) ?: HttpClient()).config {
            install(ContentNegotiation) {
                val json = this@ClientBuilder.json ?: Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                }
                json(json)

            }
            install(Resources)

            defaultRequest {
                url {
                    takeFrom(safeUrl)
                    appendPathSegments(safeUrl.pathSegments.filterNot(String::isBlank))
                }
            }
        }

        val resources = ClientResources(client, safeUrl)

        return build(resources)
    }

    protected abstract fun build(resources: ClientResources): T
}
