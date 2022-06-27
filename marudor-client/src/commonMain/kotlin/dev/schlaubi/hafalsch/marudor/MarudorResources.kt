package dev.schlaubi.hafalsch.marudor

import io.ktor.client.*
import io.ktor.http.*

public data class MarudorResources(val client: HttpClient, val url: Url) {
    public fun close() {
        client.close()
    }
}
