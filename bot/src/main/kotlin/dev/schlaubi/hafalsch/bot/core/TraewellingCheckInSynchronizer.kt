package dev.schlaubi.hafalsch.bot.core

import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.linkButton
import com.kotlindiscord.kord.extensions.utils.dm
import dev.kord.common.DiscordTimestampStyle
import dev.kord.common.toMessageFormat
import dev.kord.rest.builder.message.create.embed
import dev.schlaubi.hafalsch.bot.config.Config
import dev.schlaubi.hafalsch.bot.database.*
import dev.schlaubi.hafalsch.bot.ui.format
import dev.schlaubi.hafalsch.bot.ui.withUIContext
import dev.schlaubi.hafalsch.bot.util.detailsByJourneyId
import dev.schlaubi.hafalsch.bot.util.showTrainInfo
import dev.schlaubi.hafalsch.marudor.Marudor
import dev.schlaubi.hafalsch.marudor.entity.JourneyInformation
import dev.schlaubi.hafalsch.traewelling.Traewelling
import dev.schlaubi.hafalsch.traewelling.entity.Status
import dev.schlaubi.stdx.coroutines.parallelMapNotNull
import io.ktor.http.*
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.Clock
import mu.KotlinLogging
import org.koin.core.component.inject
import org.litote.kmongo.gte
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private val LOG = KotlinLogging.logger { }

private data class LocalizedStatus(val language: String?, val status: Status)

class TraewellingCheckInSynchronizer : RepeatingTask() {
    override val duration: Duration = 1.minutes
    private val traewelling by inject<Traewelling>()
    private val marudor by inject<Marudor>()

    override suspend fun run() {
        LOG.debug { "Importing Träwelling check ins" }
        val checkIns = Database.traewellingLogins.find(TraevellingUserLogin::expiresAt gte Clock.System.now())
            .toList()
            .parallelMapNotNull {
                val user = traewelling.auth.getUser(it.token)
                val status = traewelling.user.listEnroute(it.token)?.data
                status?.let { safeStatus ->
                    it.id to LocalizedStatus(user.data.language, safeStatus)
                }
            }
            .toMap()
        LOG.debug { "Found the following check-ins from Towelling: ${checkIns.values}" }

        val knownIds = checkIns.mapNotNull { (_, status) ->
            status.status.train.hafasId
        }

        val knownCheckins = Database.checkIns.findForJournies(knownIds)
            .groupBy(CheckIn::user)
            .mapValues { (_, value) -> value.map(CheckIn::journeyId) }

        val newCheckIns = checkIns.filter { (user, checkin) ->
            checkin.status.train.hafasId !in (knownCheckins[user] ?: emptyList())
        }

        val journeyDetails = newCheckIns.values
            .groupBy { it.status.train.hafasId }
            .keys
            .mapNotNull { marudor.detailsByJourneyId(it) }
            .associateBy(JourneyInformation::journeyId)


        checkIns.forEach { (user, checkIn) ->
            Database.checkIns.deleteNotActive(user, checkIn.status.train.hafasId)
        }

        LOG.debug { "Found the following check-ins to be new: ${newCheckIns.values}" }

        val dbCheckIns = newCheckIns.mapNotNull { (user, trip) ->
            val start = trip.status.train.origin.evaIdentifier.toString()
            val end = trip.status.train.destination.evaIdentifier.toString()
            CheckIn(
                user = user,
                traewellingId = trip.status.id,
                journeyId = trip.status.train.hafasId,
                start = start,
                end = end,
                language = trip.language,
                delays = emptyMap()
            )
        }

        coroutineScope {
            val fullCheckIns = dbCheckIns.parallelMapNotNull {
                val newDetails = journeyDetails[it.journeyId] ?: return@parallelMapNotNull null
                val state = saveState(newDetails)
                val checkIn = it.copy(delays = state.delays)

                withUIContext(checkIn.language) {
                    checkIn.sendWelcomeMessage(state, newDetails)
                }

                checkIn
            }

            if (fullCheckIns.isNotEmpty()) {
                Database.checkIns.insertMany(fullCheckIns)
            }
        }
    }

    private suspend fun CheckIn.sendWelcomeMessage(state: JourneyState, details: JourneyInformation) {
        withUIContext(language) {
            kord.getUser(user)?.dm {
                embed {
                    title = translate("welcome_aboard.title", details.train.name)
                    description = translate(
                        "welcome_aboard.description",
                        details.train.operator?.name,
                        details.train.name,
                        details.finalDestination,
                        details.stops.first { it.station.id == end }.arrival?.time?.toMessageFormat(
                            DiscordTimestampStyle.RelativeTime
                        )
                    )

                    field {
                        this.name = translate("welcome_aboard.delay.title")

                        val currentDelay = state.delays[start]
                        val delayAtExit = state.delays[end]

                        value =
                            translate(
                                "welcome_aboard.delay.description",
                                currentDelay, delayAtExit
                            )
                    }

                    val relevantMessages = state.messages
                    if (relevantMessages.isNotEmpty()) {
                        field {
                            this.name = translate("welcome_aboard.messages.title")
                            value = relevantMessages.format()
                        }
                    }

                    components {
                        showTrainInfo(details)

                        if (details.train.type?.matches("ICE?".toRegex()) == true) {
                            linkButton {
                                label = "ICE Portal"
                                url = "https://iceportal.de"
                            }
                        }
                        linkButton {
                            label = "bahn.expert"
                            url = marudor.hafas.detailsRedirect(journeyId)
                        }

                        linkButton {
                            label = "Träwelling.de"
                            url = URLBuilder(urlString = Config.TRAEWELLING_API).apply {
                                path("status", traewellingId.toString())
                            }.buildString()
                        }
                    }
                }
            }
        }
    }
}
