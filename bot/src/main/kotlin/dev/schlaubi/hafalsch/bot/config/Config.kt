package dev.schlaubi.hafalsch.bot.config

import dev.schlaubi.mikbot.plugin.api.EnvironmentConfig
import io.ktor.http.*

object Config : EnvironmentConfig() {
    val TRAEWELLING_API by getEnv(Url("https://traewelling.de/"), ::Url)
    val TRAEWELLING_CLIENT_ID by environment
    val TRAEWELLING_CLIENT_SECRET by environment
    val SYNC_TRAEWELLING by getEnv(true, String::toBooleanStrict)
}
