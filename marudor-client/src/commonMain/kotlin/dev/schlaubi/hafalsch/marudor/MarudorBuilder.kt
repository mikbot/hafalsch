package dev.schlaubi.hafalsch.marudor

import dev.schlaubi.hafalsch.client.ClientBuilder
import dev.schlaubi.hafalsch.client.ClientResources
import io.ktor.http.*

public class MarudorBuilder @PublishedApi internal constructor() : ClientBuilder<Marudor>() {
    override val defaultUrl: Url
        get() = DEFAULT_MARUDOR_URL
    override fun build(resources: ClientResources): Marudor = Marudor(resources)
}
