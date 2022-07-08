package dev.schlaubi.hafalsch.bot.core

import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.publicButton
import dev.kord.common.DiscordTimestampStyle
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.kColor
import dev.kord.common.toMessageFormat
import dev.kord.core.behavior.channel.createMessage
import dev.kord.rest.builder.message.EmbedBuilder
import dev.schlaubi.hafalsch.bot.database.*
import dev.schlaubi.hafalsch.bot.ui.*
import dev.schlaubi.hafalsch.marudor.Marudor
import dev.schlaubi.hafalsch.marudor.entity.JourneyInformation
import dev.schlaubi.mikbot.plugin.api.util.discordError
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import mu.KotlinLogging
import org.koin.core.component.inject
import org.litote.kmongo.eq
import org.litote.kmongo.`in`
import java.awt.Color
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import dev.schlaubi.mikbot.plugin.api.util.embed as makeEmbed

private val LOG = KotlinLogging.logger { }

class NotificationExecutor : RepeatingTask() {
    override val duration: Duration = 1.minutes
    private val marudor by inject<Marudor>()

    override suspend fun run() {
        val checkIns = Database.checkIns.find()
            .toList()
            .groupBy(CheckIn::journeyId)

        val currentStatuses = Database.journeyStates.find(JourneyState::journeyId `in` checkIns.keys)
            .toList()
            .associateBy(JourneyState::journeyId)


        coroutineScope {
            checkIns.forEach { (journeyId, checkIns) ->
                launch {
                    val currentSavedStatus = currentStatuses[journeyId]
                    val currentStatus = marudor.detailsByJourneyId(journeyId)
                    LOG.debug { "Found train status $currentStatus (Saved: $currentSavedStatus)" }
                    val arrivalTime = currentStatus?.stops?.last()?.arrival?.time ?: Instant.DISTANT_PAST
                    val timeSinceArrival = Clock.System.now() - arrivalTime
                    // Let's have faith in train operators and estimate that if they say a train reached it's
                    // destination for 20 minutes it actually reached its destination
                    if (currentStatus == null || timeSinceArrival >= 20.minutes) {
                        Database.journeyStates.deleteOneById(journeyId)
                        Database.checkIns.deleteMany(CheckIn::journeyId eq journeyId)
                    } else {
                        val currentState = currentStatus.toState()
                        Database.journeyStates.save(currentState)

                        if (currentSavedStatus != currentState) {
                            LOG.debug { "Sending updates to: ${checkIns.map(CheckIn::user)}" }
                            checkIns.forEach { checkIn ->
                                launch {
                                    val result = runCatching {
                                        val notificationSettings =
                                            Database.subscriptionSettings.findOneByIdSafe(checkIn.user)
                                        withUIContext(notificationSettings.locale) {
                                            sendStatus(checkIn, currentStatus, currentSavedStatus, notificationSettings)
                                        }
                                    }

                                    if (result.isFailure) {
                                        LOG.warn(result.exceptionOrNull()) { "Exception ocurred whilst processing notification" }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


private inline fun MutableList<EmbedBuilder>.embed(block: EmbedBuilder.() -> Unit) = add(makeEmbed(block))
private fun delayDifference(current: Int, before: Int?) = abs(current - (before ?: 0))

suspend fun CheckIn.saveState(marudor: Marudor): JourneyState {
    val currentStatus = marudor.detailsByJourneyId(journeyId) ?: discordError("Could not find journey details")
    val state = currentStatus.toState()
    Database.journeyStates.save(state)

    return state
}

@OptIn(KordUnsafe::class, KordExperimental::class)
private suspend fun UIContext.sendStatus(
    checkIn: CheckIn,
    currentStatus: JourneyInformation,
    previousStatus: JourneyState?,
    notificationSettings: SubscribtionSettings
) {
    suspend fun MutableList<EmbedBuilder>.addDelayChange(
        stationId: String,
        limit: Int,
        type: String
    ) {
        val stop = currentStatus.stops.firstOrNull { it.station.id == stationId } ?: return
        val arrival = stop.arrival ?: return
        val previousDelay = previousStatus?.delays?.get(stationId) ?: 0
        if (delayDifference(arrival.delay ?: 0, previousDelay) >= limit) {
            embed {
                title = translate("notification.delay_change.$type.title", currentStatus.train.name)
                description = if (stop.departure?.delay != stop.arrival?.delay) {
                    if (stop.departure != null) {
                        translate(
                            "notification.delay_change.$type.description.decrease",
                            stop.station.title,
                            arrival.delay,
                            arrival.time.toMessageFormat(DiscordTimestampStyle.RelativeTime),
                            stop.departure?.delay,
                            stop.departure?.time?.toMessageFormat(DiscordTimestampStyle.ShortTime),
                        )
                    } else {
                        translate(
                            "notification.delay_change.$type.description.no_departure",
                            stop.station.title,
                            arrival.delay,
                            arrival.time.toMessageFormat(DiscordTimestampStyle.RelativeTime)
                        )
                    }
                } else {
                    translate(
                        "notification.delay_change.$type.description",
                        stop.station.title,
                        arrival.delay,
                        arrival.time.toMessageFormat(DiscordTimestampStyle.RelativeTime),
                        currentStatus.train.operator?.name
                            ?: translate("notification.delay_change.unknown_operator")

                    )
                }

                color = if ((arrival.delay ?: 0) - previousDelay > 0) {
                    Color.RED.kColor
                } else {
                    Color.GREEN.kColor
                }

                val relevantMessages = stop.irisMessages.filterRelevant()

                if (stop.irisMessages.isNotEmpty()) {
                    field {
                        name = translate("notification.delay.likely_reasons")
                        value = relevantMessages.format()
                    }
                }

            }
        }
    }

    val embeds = buildList {
        if (notificationSettings.currentDelayMargin != 0) {
            val currentStation = currentStatus.currentStop?.station?.id
            if (currentStation != null && currentStation != checkIn.end) {
                addDelayChange(currentStation, notificationSettings.currentDelayMargin, "at_current_stop")
            }
        }
        if (notificationSettings.exitDelayMargin != 0) {
            addDelayChange(checkIn.end, notificationSettings.currentDelayMargin, "at_exit")
        }

        val newMessages = currentStatus.currentStop?.irisMessages
            ?.filter { it !in (previousStatus?.messages ?: emptyList()) }
            ?: emptyList()

        if (newMessages.isNotEmpty()) {
            embed {
                title = translate("notification.new_messages", newMessages.size, currentStatus.train.name)
                description = newMessages.filterRelevant().format()
                color = Color.RED.kColor
            }
        }
    }

    if (embeds.isNotEmpty()) {
        val channel = kord.unsafe.user(checkIn.user).getDmChannelOrNull() ?: return
        channel.createMessage {
            this.embeds.addAll(embeds)
            components {
                publicButton {
                    bundle = dev.schlaubi.hafalsch.bot.util.bundle
                    label = translate("notification.show_train_info")

                    action {
                        asUIContext {
                            journey(JourneyData(currentStatus.train.name, null, currentStatus.departure.time, null))
                        }
                    }
                }
            }
        }
    }
}

private suspend fun Marudor.detailsByJourneyId(journeyId: String): JourneyInformation? {
    val redirect = hafas.detailsRedirect(journeyId)
    val response = resoures.client.get(redirect)
    return if (response.status.isSuccess()) {
        val url = response.request.url
        //                    drop(1)/{trainName}/departure
        // https://marudor.de/details/ICE%20517/2022-07-08T05:29:00.000Z
        val (trainName, departureRaw) = url.pathSegments.drop(2)
        val departure = Instant.parse(departureRaw)
        hafas.details(trainName, date = departure)
    } else {
        null
    }
}

private fun JourneyInformation.toState() = JourneyState(
    journeyId, currentStop?.irisMessages ?: emptyList(), stops.associate { it.station.id to (it.arrival?.delay ?: 0) }
)
