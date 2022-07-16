package dev.schlaubi.hafalsch.bot.util

import com.kotlindiscord.kord.extensions.commands.converters.builders.ConverterBuilder
import com.kotlindiscord.kord.extensions.koin.KordExContext
import dev.kord.core.behavior.interaction.suggestString
import dev.schlaubi.hafalsch.marudor.Marudor
import dev.schlaubi.hafalsch.marudor.entity.JourneyInformation
import dev.schlaubi.mikbot.plugin.api.util.safeInput
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.Instant

suspend fun Marudor.detailsByJourneyId(journeyId: String): JourneyInformation? {
    val redirect = hafas.detailsRedirect(journeyId)
    val response = resoures.client.get(redirect)
    return if (response.status.isSuccess()) {
        val url = response.request.url
        //                    drop(1)/{trainName}/departure
        // https://marudor.de/details/ICE%20517/2022-07-08T05:29:00.000Z
        val (trainName, departureRaw) = url.pathSegments.drop(2)
        val departure = Instant.parse(departureRaw)
        val eva = url.parameters["stopEva"]
        hafas.details(trainName, date = departure, station = eva)
    } else {
        null
    }
}

fun ConverterBuilder<String>.journeyAutoComplete() {
    val marudor = KordExContext.get().get<Marudor>()

    autoComplete {
        val input = focusedOption.safeInput

        coroutineScope {
            val results = runCatching { marudor.hafas.journeyMatch(input) }.getOrElse { emptyList() }

            suggestString {
                results.take(25).forEach {
                    choice("${it.train.name} -> ${it.lastStop.station.title}", it.train.name)
                }
            }
        }
    }
}
