package dev.schlaubi.hafalsch.client

import io.ktor.client.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

public data class ClientResources(val client: HttpClient, val url: Url, val json: Json) {
    public fun close() {
        client.close()
    }
}
