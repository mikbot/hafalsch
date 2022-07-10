package dev.schlaubi.hafalsch.rainbow_ice

import dev.schlaubi.hafalsch.client.ClientBuilder
import dev.schlaubi.hafalsch.client.ClientResources
import dev.schlaubi.hafalsch.rainbow_ice.util.DEFAULT_RAINBOW_ICE_URL
import io.ktor.http.*

public class RainbowICEBuilder : ClientBuilder<RainbowICE>() {
    override val defaultUrl: Url = Url(DEFAULT_RAINBOW_ICE_URL)

    override fun build(resources: ClientResources): RainbowICE = RainbowICE(resources)
}
