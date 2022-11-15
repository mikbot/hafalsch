package dev.schlaubi.hafalsch.bot.core

import dev.schlaubi.hafalsch.bot.database.*
import dev.schlaubi.hafalsch.marudor.Marudor
import dev.schlaubi.hafalsch.traewelling.Traewelling
import dev.schlaubi.stdx.coroutines.parallelMapNotNull
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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
            .parallelMapNotNull {
                val status =traewelling.user.listEnroute(it.token)?.data
                status?.let { safeStatus ->
                    it.id to safeStatus
                }
            }
            .toMap()
        LOG.debug { "Found the following check-ins from Towelling: ${checkIns.values}" }

        val knownIds = checkIns.mapNotNull { (_, status) ->
            status.train.hafasId
        }

        val knownCheckins = Database.checkIns.findForJournies(knownIds)
            .groupBy(CheckIn::user)
            .mapValues { (_, value) -> value.map(CheckIn::journeyId) }

        val newCheckIns = checkIns.filter { (user, checkin) ->
            checkin.train.hafasId !in (knownCheckins[user] ?: emptyList())
        }

        checkIns.forEach { (user, checkIn) ->
            Database.checkIns.deleteNotActive(user, checkIn.train.hafasId)
        }

        LOG.debug { "Found the following check-ins to be new: ${newCheckIns.values}" }

        val dbCheckIns = newCheckIns.mapNotNull { (user, trip) ->
            val start = trip.train.origin.evaIdentifier.toString()
            val end = trip.train.destination.evaIdentifier.toString()
            CheckIn(
                user = user,
                journeyId = trip.train.hafasId,
                start = start,
                end = end
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
