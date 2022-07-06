package dev.schlaubi.hafalsch.bot.core

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.common.entity.Snowflake
import dev.schlaubi.hafalsch.bot.database.CheckIn
import dev.schlaubi.hafalsch.bot.database.Database
import dev.schlaubi.hafalsch.bot.database.TraevellingUserLogin
import dev.schlaubi.hafalsch.bot.database.findForJournies
import dev.schlaubi.hafalsch.traewelling.Traewelling
import dev.schlaubi.hafalsch.traewelling.entity.Status
import dev.schlaubi.stdx.coroutines.parallelMap
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ticker
import kotlinx.datetime.Clock
import mu.KotlinLogging
import org.koin.core.component.inject
import org.litote.kmongo.gte
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.minutes

private val LOG = KotlinLogging.logger { }

class TraewellingCheckInSynchronizer : CoroutineScope, KordExKoinComponent {

    @OptIn(ObsoleteCoroutinesApi::class)
    private val ticker = ticker(1.minutes.inWholeMilliseconds, 0)
    private lateinit var runner: Job
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()
    private val traewelling by inject<Traewelling>()

    fun start() {
        runner = launch {
            for (unit in ticker) {
                syncTraewelling()
            }
        }
    }

    private suspend fun syncTraewelling() {
        LOG.debug { "Importing Träwelling check ins" }
        val checkIns = Database.traewellingLogins.find(TraevellingUserLogin::expiresAt gte Clock.System.now())
            .toList()
            .parallelMap {
                it.id to traewelling.statuses.listEnroute(it.token).statuses
            }
            .toMap()
        LOG.debug { "Found the following check-ins from Träwelling: ${checkIns.values}" }

        val knownIds = checkIns.flatMap { (_, status) ->
            status.map { it.trainCheckin.tripId }
        }

        val knownCheckins = Database.checkIns.findForJournies(knownIds)
            .groupBy(CheckIn::user)
            .mapValues { (_, value) -> value.map(CheckIn::journeyId) }

        val newCheckIns = checkIns.flatMap { (user, trips) ->
            trips.filter {
                it.trainCheckin.tripId !in (knownCheckins[user] ?: emptyList())
            }.map {
                user to it
            }
        }

        LOG.debug { "Found the following check-ins to be new: ${newCheckIns.map(Pair<Snowflake, Status>::second)}" }

        val dbCheckIns = newCheckIns.map { (user, trip) ->
            CheckIn(
                user = user,
                journeyId = trip.trainCheckin.tripId,
                start = trip.trainCheckin.originStopOver.trainStation.ibnr.toString(),
                end = trip.trainCheckin.destinationStopOver.trainStation.ibnr.toString()
            )
        }

        if (dbCheckIns.isNotEmpty()) {
            Database.checkIns.insertMany(dbCheckIns)
        }
    }
}
