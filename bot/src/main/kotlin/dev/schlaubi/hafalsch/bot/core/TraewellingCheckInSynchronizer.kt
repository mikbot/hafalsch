package dev.schlaubi.hafalsch.bot.core

import dev.kord.common.entity.Snowflake
import dev.schlaubi.hafalsch.bot.database.CheckIn
import dev.schlaubi.hafalsch.bot.database.Database
import dev.schlaubi.hafalsch.bot.database.TraevellingUserLogin
import dev.schlaubi.hafalsch.bot.database.findForJournies
import dev.schlaubi.hafalsch.marudor.Marudor
import dev.schlaubi.hafalsch.traewelling.Traewelling
import dev.schlaubi.hafalsch.traewelling.entity.Status
import dev.schlaubi.stdx.coroutines.parallelMap
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import mu.KotlinLogging
import org.koin.core.component.inject
import org.litote.kmongo.gte
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private val LOG = KotlinLogging.logger { }

class TraewellingCheckInSynchronizer : RepeatingTask() {

    override val duration: Duration = 1.minutes
    private val traewelling by inject<Traewelling>()
    private val marudor by inject<Marudor>()

    override suspend fun run() {
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

        coroutineScope {
            dbCheckIns.forEach {
                launch {
                    it.saveState(marudor)
                }
            }
            if (dbCheckIns.isNotEmpty()) {
                Database.checkIns.insertMany(dbCheckIns)
            }
        }
    }
}
