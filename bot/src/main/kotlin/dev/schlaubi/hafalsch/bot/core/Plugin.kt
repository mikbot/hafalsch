package dev.schlaubi.hafalsch.bot.core

import ch.qos.logback.classic.Level
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.loadModule
import dev.schlaubi.hafalsch.bot.commands.*
import dev.schlaubi.hafalsch.bot.commands.traewelling.traewellingCommand
import dev.schlaubi.hafalsch.bot.config.Config
import dev.schlaubi.hafalsch.client.invoke
import dev.schlaubi.hafalsch.marudor.Marudor
import dev.schlaubi.hafalsch.rainbow_ice.RainbowICE
import dev.schlaubi.hafalsch.traewelling.Traewelling
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.PluginWrapper
import dev.schlaubi.mikbot.plugin.api.util.discordError
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.statement.*
import kotlinx.coroutines.cancel
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.minutes
import dev.schlaubi.mikbot.plugin.api.config.Config as BotConfig


@Serializable
private data class TraewellingError(val error: String)

@PluginMain
class Plugin(wrapper: PluginWrapper) : Plugin(wrapper) {
    private val traewellingSynchronizer = TraewellingCheckInSynchronizer()
    private val notificationExecutor = NotificationExecutor()
    private val baseClient = HttpClient {
        Logging {
            level = when(BotConfig.LOG_LEVEL) {
                Level.TRACE -> LogLevel.ALL
                Level.DEBUG -> LogLevel.BODY
                else -> LogLevel.NONE
            }
            logger = Logger.DEFAULT
        }
    }

    private val marudor = Marudor {
        httpClient = baseClient
    }
    private val rainbowICE = RainbowICE()

    private val traewelling = Traewelling {
        url(Config.TRAEWELLING_API)
        httpClient = baseClient.config {
            expectSuccess = true
            HttpResponseValidator {
                handleResponseExceptionWithRequest { exception, _ ->
                    val clientException =
                        exception as? ClientRequestException ?: return@handleResponseExceptionWithRequest
                    val exceptionResponse = clientException.response

                    val body = exceptionResponse.bodyAsText()
                    val message =
                        runCatching { Json.decodeFromString<TraewellingError>(body).error }.getOrElse { body }
                    discordError(message)
                }
            }

            install(HttpTimeout) {
                // Träwelling requests can take long
                requestTimeoutMillis = 1.minutes.inWholeMilliseconds
            }
        }
    }

    override suspend fun ExtensibleBotBuilder.apply() {
        hooks {
            afterKoinSetup {
                loadModule {
                    single { marudor }
                    single { traewelling }
                    single { rainbowICE }
                }

//                traewellingSynchronizer.start()
                notificationExecutor.start()
            }
        }
    }

    override fun stop() {
        traewellingSynchronizer.cancel()
        notificationExecutor.cancel()
    }

    override fun ExtensibleBotBuilder.ExtensionsBuilder.addExtensions() {
        add(::HafalschModule)
    }
}

class HafalschModule : Extension() {
    override val name: String = "Hafalsch"
    override val bundle: String = dev.schlaubi.hafalsch.bot.util.bundle
    val rainbowICE by inject<RainbowICE>()
    val marudor by inject<Marudor>()
    val traewelling by inject<Traewelling>()

    override suspend fun setup() {
        stationCommand()
        journeyCommand()
        departuresCommand()
        notificationSettingsCommand()
        tznCommand()
        traewellingCommand()
    }
}
