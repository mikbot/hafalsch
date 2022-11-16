package dev.schlaubi.hafalsch.bot.util

import com.kotlindiscord.kord.extensions.commands.converters.builders.ConverterBuilder
import com.kotlindiscord.kord.extensions.koin.KordExContext
import dev.kord.core.behavior.interaction.suggestString
import dev.schlaubi.hafalsch.marudor.Marudor
import dev.schlaubi.hafalsch.marudor.entity.JourneyInformation
import dev.schlaubi.mikbot.plugin.api.util.safeInput
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.Instant

private val notRedirectingClient = HttpClient {
    followRedirects = false
}

suspend fun Marudor.detailsByJourneyId(journeyId: String): JourneyInformation? {
    val redirect = hafas.detailsRedirect(journeyId)
    val response = notRedirectingClient.get(redirect)
    return if (response.status == HttpStatusCode.Found) {
        val url = response.headers[HttpHeaders.Location]?.let(::Url) ?: return null
        //             drop(1)/{trainName}/departure
        // Location: /details/ICE%20517/2022-07-08T05:29:00.000Z
        val (trainName, departureRaw) = url.pathSegments.drop(2)
        val departure = Instant.parse(departureRaw)
        val eva = url.parameters["stopEva"]
        journeys.details(trainName, initialDepartureDate = departure, evaNumberAlongRoute = eva)
    } else {
        null
    }
}

fun ConverterBuilder<String>.journeyAutoComplete() {
    val marudor = KordExContext.get().get<Marudor>()

    autoComplete {
        val input = focusedOption.safeInput

        coroutineScope {
            val results = runCatching { marudor.journeys.find(input) }.getOrElse { emptyList() }

            suggestString {
                results.take(25).forEach {
                    choice("${it.train.name} -> ${it.lastStop.station.title}", it.train.name)
                }
            }
        }
    }
}
