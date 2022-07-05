package dev.schlaubi.hafalsch.traewelling

import dev.schlaubi.hafalsch.client.ClientBuilder
import dev.schlaubi.hafalsch.client.ClientResources
import io.ktor.http.*

public class TraewellingBuilder : ClientBuilder<Traewelling>() {
    override val defaultUrl: Url
        get() = DEFAULT_TRAEWELLING_URL

    override fun build(resources: ClientResources): Traewelling = Traewelling(resources)
}
