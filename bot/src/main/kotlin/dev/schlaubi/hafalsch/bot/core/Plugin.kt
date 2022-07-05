package dev.schlaubi.hafalsch.bot.core

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.loadModule
import dev.schlaubi.hafalsch.bot.commands.departuresCommand
import dev.schlaubi.hafalsch.bot.commands.journeyCommand
import dev.schlaubi.hafalsch.bot.commands.stationCommand
import dev.schlaubi.hafalsch.bot.commands.traewelling.traewellingCommand
import dev.schlaubi.hafalsch.bot.config.Config
import dev.schlaubi.hafalsch.client.invoke
import dev.schlaubi.hafalsch.marudor.Marudor
import dev.schlaubi.hafalsch.traewelling.Traewelling
import dev.schlaubi.mikbot.plugin.api.Plugin
import dev.schlaubi.mikbot.plugin.api.PluginMain
import dev.schlaubi.mikbot.plugin.api.PluginWrapper
import dev.schlaubi.mikbot.plugin.api.util.discordError
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.statement.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


@Serializable
private data class TraewellingError(val error: String)

@PluginMain
class Plugin(wrapper: PluginWrapper) : Plugin(wrapper) {
    private val marudor = Marudor()
    private val traewelling = Traewelling {
        url(Config.TRÄWELLING_API)
        httpClient = HttpClient {
            expectSuccess = true
            Logging {
                level = LogLevel.ALL
                logger = object : Logger {
                    override fun log(message: String) = println(message)
                }
            }

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
        }
    }

    override suspend fun ExtensibleBotBuilder.apply() {
        hooks {
            afterKoinSetup {
                loadModule {
                    single { marudor }
                    single { traewelling }
                }
            }
        }
    }

    override fun ExtensibleBotBuilder.ExtensionsBuilder.addExtensions() {
        add(::HafalschModule)
    }
}

class HafalschModule : Extension() {
    override val name: String = "Hafalsch"
    override val bundle: String = "hafalsch"

    override suspend fun setup() {
        stationCommand()
        journeyCommand()
        departuresCommand()
        traewellingCommand()
    }
}
