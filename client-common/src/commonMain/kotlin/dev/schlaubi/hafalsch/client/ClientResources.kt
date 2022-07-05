package dev.schlaubi.hafalsch.client

import io.ktor.client.*
import io.ktor.http.*

public data class ClientResources(val client: HttpClient, val url: Url) {
    public fun close() {
        client.close()
    }
}
