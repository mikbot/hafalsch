package dev.schlaubi.hafalsch.bot.config

import dev.schlaubi.mikbot.plugin.api.EnvironmentConfig

object Config : EnvironmentConfig() {
    val TRÄWELLING_API by getEnv("https://traewelling.de/")
}
