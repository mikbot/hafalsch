package dev.schlaubi.hafalsch.bot.util

import dev.schlaubi.hafalsch.marudor.Marudor
import dev.schlaubi.hafalsch.marudor.entity.CoachSequence
import dev.schlaubi.hafalsch.marudor.entity.JourneyInformation
import dev.schlaubi.hafalsch.marudor.entity.Stop

context(Marudor)
        suspend fun JourneyInformation.fetchCoachSequence(stop: Stop): CoachSequence? {
    return coachSequence.coachSequence(
        train.number?.toInt() ?: return null,
        stop.departure?.scheduledTime ?: return null,
        stop.station.id,
        stop.departure?.scheduledTime
    )
}
