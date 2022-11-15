package dev.schlaubi.hafalsch.bot.config

import dev.schlaubi.mikbot.plugin.api.EnvironmentConfig

object Config : EnvironmentConfig() {
    val TRAEWELLING_API by getEnv("https://traewelling.de/")
}
